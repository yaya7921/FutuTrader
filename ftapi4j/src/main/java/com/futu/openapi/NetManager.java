package com.futu.openapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接错误类型
 */
enum ConnErr {
    /**
     * 没有错误
     */
    OK,
    /**
     * 连接失败
     */
    CONNECT_FAIL,
    /**
     * 连接超时
     */
    CONNECT_TIMEOUT,
    /**
     * 发送失败
     */
    SEND_FAIL,
    /**
     * 接收失败
     */
    READ_FAIL,
    /**
     * 主动关闭
     */
    CLOSE,
    /**
     * 远端已关闭
     */
    REMOTE_CLOSE
}


/**
 * 连接回调
 */
interface ConnHandler {
    /**
     * 连接回调
     * @param connID 连接id
     * @param err 连接是否成功
     * @param msg 错误描述
     */
    void onConnect(long connID, ConnErr err, String msg);

    /**
     * 连接断开回调
     * @param connID 连接id
     * @param err 连接是否成功
     * @param msg 错误描述
     */
    void onDisConnect(long connID, ConnErr err, String msg);

    /**
     * 收包回调
     * @param connID  连接id
     * @param data 收到的数据缓存
     * @param offset 本次收到的数据起始位置
     * @param count  收到的字节数
     */
    void onRecv(long connID, byte[] data, int offset, int count);

    /**
     * 每秒定时回调
     */
    void onTick();
}


/**
 * 记录每个连接的数据
 */
class ConnData {
    long connID = 0;
    SocketChannel socketChannel;
    SelectionKey selKey;
    ConnHandler handler;
    ByteBuffer readBuf = ByteBuffer.allocate(64*1024);
    ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
    long startConnectMS;
    int connectTimeoutMS;
}


/**
 * 网络请求类型
 */
enum ConnReqType {
    CONNECT,
    SEND,
}


/**
 * 网络请求参数
 */
class ConnReq {
    long connID;
    ConnReqType reqType;
    byte[] data;
    int connectTimeoutMS;
    ConnHandler handler;
    InetSocketAddress remoteAddr;

    ConnReq(ConnReqType type, long connID) {
        reqType = type;
        this.connID = connID;
    }
}


/**
 * 管理网络连接、收发
 */
public class NetManager {
    Thread pollThread;
    Selector selector;
    AtomicLong nextConnID = new AtomicLong(1);
    ConcurrentLinkedQueue<ConnReq> reqQueue = new ConcurrentLinkedQueue<>();
    ConcurrentHashMap.KeySetView<Long, Boolean> closingConnSet = ConcurrentHashMap.newKeySet();
    ConcurrentHashMap<Long, ConnData> connDataMap = new ConcurrentHashMap<>();
    PriorityQueue<ConnData> connectingConnQueue = new PriorityQueue<>((ConnData lhs, ConnData rhs) -> {
        long lhsDeadline = lhs.startConnectMS + lhs.connectTimeoutMS;
        long rhsDeadline = rhs.startConnectMS + rhs.connectTimeoutMS;
        if (lhsDeadline < rhsDeadline)
            return -1;
        else if (lhsDeadline == rhsDeadline)
            return 0;
        return 1;
    });

    final private static NetManager defaultInstance = new NetManager();

    NetManager() {
        try {
            selector = Selector.open();
            pollThread = new Thread(this::loop);
            pollThread.setDaemon(true);
            pollThread.start();
        } catch (IOException ex) {
            throw new RuntimeException("Fail to create NetManager", ex);
        }
    }


    /**
     * 单例
     * @return 单例
     */
    static NetManager getInstance() {
        return defaultInstance;
    }

    /**
     * io线程的循环
     */
    void loop() {
        long lastTickTime = Clock.systemUTC().millis();
        while (true) {
            try {
                int count = selector.select(50);
                handleCloseReqs();
                handleReqs();
                if (count > 0) {
                    handleNetEvents(selector.selectedKeys());
                }
                long now = Clock.systemUTC().millis();
                if (now - lastTickTime >= 1000) {
                    notifyTick();
                    handleConnectingConns();
                    lastTickTime = now;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 发起连接请求
     * @param timeout 超时，单位ms。必须大于0
     * @param handler 回调，必须不是null
     * @return 连接id，即后续请求所需的connID参数
     */
    long connect(InetSocketAddress addr, int timeout, ConnHandler handler) {
        if (timeout <= 0)
            throw new IllegalArgumentException("timeout should be greater than 0");
        if (handler == null)
            throw new NullPointerException("handler is null");

        long connID = nextConnID.getAndIncrement();
        ConnReq connReq = new ConnReq(ConnReqType.CONNECT, connID);
        connReq.remoteAddr = addr;
        connReq.connectTimeoutMS = timeout;
        connReq.handler = handler;
        reqQueue.add(connReq);
        selector.wakeup();
        return connID;
    }

    /**
     * 发送数据
     * @param connID 连接id
     * @param data 要发送的数据
     */
    void send(long connID, byte[] data) {
        if (connID <= 0)
            throw new IllegalArgumentException("connID should be greater than 0");
        if (data == null || data.length == 0)
            throw new IllegalArgumentException("no data to send");

        ConnReq req = new ConnReq(ConnReqType.SEND, connID);
        req.data = data;
        reqQueue.add(req);
        selector.wakeup();
    }

    /**
     * 关闭连接。关闭后会调用ConnHandler.onDisconnect，ConnErr.CLOSE
     * @param connID 连接id
     */
    void close(long connID) {
        if (connID <= 0)
            throw new IllegalArgumentException("connID should be greater than 0");

        closingConnSet.add(connID);
        selector.wakeup();
    }

    private void handleReqs() {
        for (ConnReq req = reqQueue.poll(); req != null; req = reqQueue.poll()) {
            if (req.reqType == ConnReqType.CONNECT) {
                handleConnect(req);
            } else if (req.reqType == ConnReqType.SEND) {
                handleSend(req);
            }
        }
    }

    private void handleConnect(ConnReq req) {
        SelectionKey selKey = null;
        try {
            SocketChannel sockChan = SocketChannel.open();
            sockChan.configureBlocking(false);
            sockChan.setOption(StandardSocketOptions.TCP_NODELAY, true);
            selKey = sockChan.register(selector, SelectionKey.OP_CONNECT);
            ConnData connData = new ConnData();
            connData.handler = req.handler;
            connData.connID = req.connID;
            connData.connectTimeoutMS = req.connectTimeoutMS;
            connData.selKey = selKey;
            connData.socketChannel = sockChan;
            connData.startConnectMS = Clock.systemUTC().millis();
            selKey.attach(req.connID);
            connDataMap.put(connData.connID, connData);
            boolean isConnected = sockChan.connect(req.remoteAddr);
            if (isConnected) {
                onConnect(selKey);
            } else {
                connectingConnQueue.add(connData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (selKey != null) {
                selKey.cancel();
                connDataMap.remove(req.connID);
            }

            try {
                if (req.handler != null) {
                    req.handler.onConnect(req.connID, ConnErr.CONNECT_FAIL, ex.getMessage());
                }
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }

    private void handleSend(ConnReq req) {
        ConnData connData = connDataMap.getOrDefault(req.connID, null);
        if (connData == null)
            return;
        if (!connData.socketChannel.isConnected()) {
            assert false : String.format("Send to non-connected connection: connID=%d", connData.connID);
            return;
        }
        if (req.data == null || req.data.length == 0)
            return;

        if (connData.writeStream.size() == 0) {
            try {
                ByteBuffer buf = ByteBuffer.wrap(req.data);
                int sentCount = connData.socketChannel.write(buf);
                if (sentCount < req.data.length) {
                    connData.writeStream.write(req.data, sentCount, req.data.length-sentCount);
                    enableWrite(connData.selKey, true);
                }
            } catch (IOException ex) {
                onClose(req.connID, ConnErr.SEND_FAIL, ex.getMessage(), true);
            }
        } else {
            try {
                connData.writeStream.write(req.data, 0, req.data.length);
            } catch (Exception ex) {
                onClose(req.connID, ConnErr.SEND_FAIL, ex.getMessage(), true);
            }
        }
    }

    private void handleCloseReqs() {
        Iterator<Long> iter = closingConnSet.iterator();
        while (iter.hasNext()) {
            long connID = iter.next();
            onClose(connID, ConnErr.CLOSE, "", true);
            iter.remove();
        }
    }

    private void handleConnectingConns() {
        long now = Clock.systemUTC().millis();
        while (true) {
            ConnData connData = connectingConnQueue.peek();
            if (connData == null)
                break;
            if (connData.socketChannel.isConnectionPending()) {
                if (connData.startConnectMS + connData.connectTimeoutMS < now) {
                    connData.handler.onConnect(connData.connID, ConnErr.CONNECT_TIMEOUT, "Connect timeout");
                    onClose(connData.connID, ConnErr.CONNECT_TIMEOUT, "", false);
                    connectingConnQueue.poll();
                } else {
                    break;
                }
            } else {
                connectingConnQueue.poll();
            }
        }
    }

    private void onClose(long connID, ConnErr err, String msg, boolean bNotify) {
        ConnData connData = connDataMap.getOrDefault(connID, null);
        if (connData == null) {
            return;
        }

        try {
            connData.selKey.cancel();
            connData.socketChannel.close();
        } catch (IOException ignore) {

        }

        if (bNotify) {
            try {
                connData.handler.onDisConnect(connID, err, msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        connDataMap.remove(connID);
        connectingConnQueue.remove(connData);
    }

    private void handleNetEvents(Set<SelectionKey> keys) {
        for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
            SelectionKey key = iter.next();
            if (key.isConnectable())
                onConnect(key);
            if (key.isValid() && key.isReadable())
                onRead(key);
            if (key.isValid() && key.isWritable())
                onWrite(key);
            iter.remove();
        }
    }

    private void notifyTick() {
        connDataMap.forEach((Long connID, ConnData connData) -> {
            if (connData.socketChannel.isConnected())
                connData.handler.onTick();
        });
    }

    private void onConnect(SelectionKey selKey) {
        long connID = (long)selKey.attachment();
        ConnData connData = connDataMap.getOrDefault(connID, null);
        ConnErr err = ConnErr.OK;
        String msg = "";

        if (connData == null)
            return;

        connectingConnQueue.remove(connData);
        try {
            connData.socketChannel.finishConnect();
            selKey.interestOps(SelectionKey.OP_READ);
        } catch (IOException ex) {
            ex.printStackTrace();
            err = ConnErr.CONNECT_FAIL;
            msg = ex.getMessage();
        }

        try {
            connData.handler.onConnect(connID, err, msg);
        } catch (Exception ex) {
            ex.printStackTrace();
            err = ConnErr.CONNECT_FAIL;
            msg = ex.getMessage();
        }

        if (err != ConnErr.OK) {
            onClose(connID, err, msg, false);
        }
    }

    private void onRead(SelectionKey selKey) {
        long connID = (long)selKey.attachment();
        ConnData connData = connDataMap.getOrDefault(connID, null);
        ConnErr err = ConnErr.OK;
        String msg = "";
        int readCount = 0;

        if (connData == null)
            return;

        if (!connData.socketChannel.isConnected())
            return;

        try {
            readCount = connData.socketChannel.read(connData.readBuf);
            if (readCount > 0) {
                connData.readBuf.flip();
                connData.handler.onRecv(connID, connData.readBuf.array(), 0, readCount);
                connData.readBuf.clear();
            } else if (readCount < 0) {
                onClose(connID, ConnErr.REMOTE_CLOSE, "", true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            err = ConnErr.READ_FAIL;
            msg = ex.getMessage();
            onClose(connID, err, msg, true);
        }
    }

    private void onWrite(SelectionKey selKey) {
        long connID = (long)selKey.attachment();
        ConnData connData = connDataMap.getOrDefault(connID, null);
        ConnErr err = ConnErr.OK;
        String msg = "";
        int writeCount;

        if (connData == null)
            return;

        if (!connData.socketChannel.isConnected())
            return;

        try {
            byte[] data = connData.writeStream.toByteArray();
            connData.writeStream.reset();
            ByteBuffer buf = ByteBuffer.wrap(data);
            writeCount = connData.socketChannel.write(buf);
            if (writeCount < data.length) {
                connData.writeStream.write(data, writeCount, data.length - writeCount);
            } else {
                connData.selKey.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException ex) {
            err = ConnErr.SEND_FAIL;
            msg = ex.getMessage();
        }

        if (err != ConnErr.OK) {
            onClose(connID, err, msg, true);
        }
    }

    private void enableRead(SelectionKey key, boolean isEnable) {
        int ops = key.interestOps();
        if (isEnable)
            ops = ops | SelectionKey.OP_READ;
        else
            ops = ops & (~SelectionKey.OP_READ);

        key.interestOps(ops);
    }

    private void enableWrite(SelectionKey key, boolean isEnable) {
        int ops = key.interestOps();
        if (isEnable)
            ops = ops | SelectionKey.OP_WRITE;
        else
            ops = ops & (~SelectionKey.OP_WRITE);

        key.interestOps(ops);
    }
}
