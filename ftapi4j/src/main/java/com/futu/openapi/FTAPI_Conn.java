package com.futu.openapi;

import com.futu.openapi.pb.*;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


class SentProtoData {
    ProtoHeader header;
    long sentTime;
    Semaphore sem;

    SentProtoData(boolean isSync) {
        if (isSync)
            sem = new Semaphore(0);
    }
}

/**
 * 表示和OpenD的一条连接
 */
public class FTAPI_Conn implements AutoCloseable, ConnHandler {
    public static final int INIT_FAIL = 100;
    public static final int REPLY_TIMEOUT = 12 * 1000; //12s
    public static final int CONNECT_TIMEOUT = 10 * 1000;

    protected long localConnID;
    private FTSPI_Conn connSpi;
    final private Object connSpiLock = new Object();
    String clientID = "";
    int clientVer = 0;
    KeyPair rsaKeyPair = null;
    AesCbcCipher cipher;
    AtomicInteger nextPacketSN = new AtomicInteger(1);
    ConnStatus connStatus = ConnStatus.START;
    String ip = "";
    int port;
    boolean isEncrypt = false;
    HashMap<Integer, SentProtoData> sentProtoMap = new HashMap<>();
    SimpleBuffer readBuf = new SimpleBuffer(64*1024);
    long connID;
    long userID;
    String aesKey = "";
    String aesCBIV = "";
    int keepAliveInterval = 9 * 1000;  //心跳间隔，单位ms
    long lastKeepAliveTime = 0; //上次心跳时间，单位ms

    public FTAPI_Conn() {

    }

    /**
     * 设置连接回调
     * @param callback 回调
     */
    public void setConnSpi(FTSPI_Conn callback) {
        synchronized (connSpiLock) {
            connSpi = callback;
        }
    }

    /***
     * 关闭连接
     */
    public void close() {
        synchronized (this) {
            if (connStatus != ConnStatus.START && connStatus != ConnStatus.CLOSED) {
                NetManager.getInstance().close(localConnID);
            } else {
                connStatus = ConnStatus.CLOSED;
            }
        }
    }

    /***
     * 断开与OpenD的连接
     * @return 是否成功
     * @deprecated 已经废弃，统一使用close关闭
     */
    @Deprecated
    public boolean disconnect() {
        close();
        return true;
    }

    /***
     * 设置客户端信息，用于记录
     * @param clientID 客户端名字
     * @param clientVer 客户端版本
     */
    public void setClientInfo(String clientID, int clientVer) {
        synchronized (this) {
            this.clientID = clientID;
            this.clientVer = clientVer;
        }
    }

    /***
     * 设置加密私钥
     * @param key 私钥字符串
     */
    public void setRSAPrivateKey(String key) {
        byte[] binaryKey = key.getBytes(StandardCharsets.UTF_8);

        synchronized (this) {
            try{
                rsaKeyPair = RsaUtil.loadKeyPairFromArray(binaryKey);
            } catch (IOException ex) {
                throw new APIError(String.format("Invalid rsa private key: %s", ex.getMessage()), ex);
            }
        }
    }

    /**
     * 连接并初始化
     * @param ip              地址
     * @param port            端口
     * @param isEnableEncrypt 启用加密
     * @return bool 是否启动了执行，不代表连接结果，结果通过OnInitConnect回调
     */
    public boolean initConnect(String ip, int port, boolean isEnableEncrypt) {
        if (ip == null || ip.isEmpty()) {
            throw new IllegalArgumentException("ip is invalid");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port is invalid");
        }

        synchronized (this) {
            this.ip = ip;
            this.port = port;
            this.isEncrypt = isEnableEncrypt;
            this.localConnID = NetManager.getInstance().connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT, this);
        }
        return true;
    }

    /**
     * 此连接的连接ID，连接的唯一标识，InitConnect协议返回，没有初始化前为0
     * @return 连接id
     */
    public long getConnectID() {
        synchronized (this) {
            return this.connID;
        }
    }

    /**
     * 发送协议数据
     * @param protoID 协议id，@see ProtoID
     * @param req proto数据包
     * @return 数据包序列号
     */
    protected int sendProto(int protoID, GeneratedMessageV3 req) {
        synchronized (this) {
            if (connStatus != ConnStatus.CONNECTED && connStatus != ConnStatus.READY)
                return 0;

            byte[] body = req.toByteArray();
            byte[] bodySha1;

            try {
                bodySha1 = SHA1Util.calc(body);
                if (protoID == ProtoID.INIT_CONNECT) {
                    if (rsaKeyPair != null) {
                        body = RsaUtil.encrypt(body, rsaKeyPair.getPublic());
                    }
                } else if (isEncrypt && cipher != null) {
                    body = cipher.encrypt(body);
                }
            } catch (NoSuchAlgorithmException ex) {
                throw new APIError(String.format("Calc body sha1 fail: %s", ex.getMessage()), ex);
            } catch (GeneralSecurityException ex) {
                throw new APIError(String.format("RSA encrypt fail: %s", ex.getMessage()), ex);
            }

            ProtoHeader header = new ProtoHeader();
            header.nProtoID = protoID;
            header.nProtoFmtType = (byte)0;
            header.nProtoVer = 0;
            header.nSerialNo = nextPacketSN.getAndIncrement();
            header.nBodyLen = body.length;
            header.arrBodySHA1 = bodySha1;

            SentProtoData sentData = new SentProtoData(false);
            sentData.header = header;
            sentData.sentTime = Clock.systemUTC().millis();
            sentProtoMap.put(header.nSerialNo, sentData);

            byte[] buffer = new byte[ProtoHeader.HEADER_SIZE + body.length];
            header.write(buffer);
            System.arraycopy(body, 0, buffer, ProtoHeader.HEADER_SIZE, body.length);
            NetManager.getInstance().send(localConnID, buffer);
            return header.nSerialNo;
        }
    }

    /**
     * 发送的协议收到了返回
     * @param replyType 表示返回数据的错误类型
     * @param protoHeader 包头
     * @param data 包体
     */
    protected void onReply(ReqReplyType replyType, ProtoHeader protoHeader, byte[] data) {

    }

    /**
     * 收到OpenD的推送
     * @param protoHeader 包头
     * @param data 包体
     */
    protected void onPush(ProtoHeader protoHeader, byte[] data) {

    }

    private void onInitConnect(long errCode, String desc) {
        synchronized (this) {
            if (errCode == 0) {
                connStatus = ConnStatus.READY;
            } else {
                close();
            }
        }

        synchronized (connSpiLock) {
            if (connSpi != null) {
                connSpi.onInitConnect(this, errCode, desc);
            }
        }
    }

    @Override
    public final void onConnect(long connID, ConnErr err, String msg) {
        synchronized (this) {
            if (err == ConnErr.OK) {
                connStatus = ConnStatus.CONNECTED;
                sendInitConnect();
                return;
            } else {
                connStatus = ConnStatus.CLOSED;
            }
        }

        synchronized (connSpiLock) {
            if (connSpi != null) {
                long errCode = makeInitConnectErrCode(connErrToConnectFailType(err), 0);
                connSpi.onInitConnect(this, errCode, msg);
            }
        }
    }

    @Override
    public final void onDisConnect(long connID, ConnErr err, String msg) {
        synchronized (this) {
            connStatus = ConnStatus.CLOSED;
        }

        synchronized (connSpiLock) {
            if (connSpi != null) {
                long errCode = makeInitConnectErrCode(connErrToConnectFailType(err), 0);
                connSpi.onDisconnect(this, errCode);
            }
        }
    }

    @Override
    public final void onRecv(long connID, byte[] data, int offset, int count) {
        synchronized (this) {
            for (int remain = count; remain > 0; ) {
                int readCount = readBuf.append(data, offset, remain);
                offset += readCount;
                remain -= readCount;
                handleReadBuf();
            }
        }
    }

    @Override
    public void onTick() {
        long now = Clock.systemUTC().millis();
        synchronized (this) {
            if (now - lastKeepAliveTime >= keepAliveInterval && connStatus == ConnStatus.READY) {
                sendKeepAlive();
                lastKeepAliveTime = now;
            }

            Set<Map.Entry<Integer, SentProtoData>> items = sentProtoMap.entrySet();
            for (Iterator<Map.Entry<Integer, SentProtoData>> iter = items.iterator(); iter.hasNext(); ) {
                Map.Entry<Integer, SentProtoData> item = iter.next();
                SentProtoData protoData = item.getValue();
                if (now - protoData.sentTime > REPLY_TIMEOUT) {
                    handleReplyPacket(ReqReplyType.Timeout, protoData.header, null, false);
                    iter.remove();
                }
            }
        }
    }

    private void handleReadBuf() {
        while (ProtoHeader.HEADER_SIZE <= readBuf.length) {
            ProtoHeader header = ProtoHeader.parse(readBuf.buf, readBuf.start);
            if (header == null)
                break;
            if (header.nBodyLen + ProtoHeader.HEADER_SIZE > readBuf.limit) {
                readBuf.resize(header.nBodyLen + ProtoHeader.HEADER_SIZE);
            }

            if (ProtoHeader.HEADER_SIZE + header.nBodyLen > readBuf.length)
                break;
            readBuf.consume(ProtoHeader.HEADER_SIZE);
            byte[] body = null;
            ReqReplyType replyType = ReqReplyType.SvrReply;
            if (header.nBodyLen > 0) {
                body = new byte[header.nBodyLen];
                System.arraycopy(readBuf.buf, readBuf.start, body, 0, header.nBodyLen);

                try {
                    if (isEncrypt) {
                        if (header.nProtoID == ProtoID.INIT_CONNECT) {
                            body = RsaUtil.decrypt(body, rsaKeyPair.getPrivate());
                        } else {
                            body = cipher.decrypt(body);
                        }
                    }
                } catch (Exception ex) {
                    body = null;
                    replyType = ReqReplyType.Invalid;
                } finally {
                    readBuf.consume(header.nBodyLen);
                }
            }

            if (ProtoID.isPushProto(header.nProtoID)) {
                if (replyType == ReqReplyType.SvrReply) {
                    handlePushPacket(header, body);
                }
            } else {
                handleReplyPacket(replyType, header, body, true);
            }
        }
    }

    private void handleReplyPacket(ReqReplyType replyType, ProtoHeader header, byte[] body, boolean isRemoveFromSent) {
        SentProtoData sentData = sentProtoMap.getOrDefault(header.nSerialNo, null);
        if (sentData == null)
            return;
        if (sentData.header.nProtoID != header.nProtoID)
            return;
        if (isRemoveFromSent)
            sentProtoMap.remove(header.nSerialNo);
        if (header.nProtoID == ProtoID.INIT_CONNECT)
            handleInitConnect(replyType, header, body);
        else
            onReply(replyType, header, body);
    }

    private int sendInitConnect() {
        InitConnect.C2S.Builder c2s = InitConnect.C2S.newBuilder().setClientVer(clientVer)
                .setClientID(clientID)
                .setRecvNotify(true)
                .setPushProtoFmt(Common.ProtoFmt.ProtoFmt_Protobuf_VALUE)
                .setProgrammingLanguage("Java");
        if (isEncrypt)
            c2s.setPacketEncAlgo(Common.PacketEncAlgo.PacketEncAlgo_AES_CBC_VALUE);
        else
            c2s.setPacketEncAlgo(Common.PacketEncAlgo.PacketEncAlgo_None_VALUE);
        InitConnect.Request req = InitConnect.Request.newBuilder().setC2S(c2s).build();
        return sendProto(ProtoID.INIT_CONNECT, req);
    }

    private int sendKeepAlive() {
        KeepAlive.C2S c2s = KeepAlive.C2S.newBuilder().setTime(Clock.systemUTC().millis() / 1000).build();
        KeepAlive.Request req = KeepAlive.Request.newBuilder().setC2S(c2s).build();
        return sendProto(ProtoID.KEEP_ALIVE, req);
    }

    private void handleInitConnect(ReqReplyType replyType, ProtoHeader header, byte[] body) {
        InitConnect.Response rsp;
        long errCode = 0;
        String desc = "";
        if (replyType == ReqReplyType.SvrReply) {
            try {
                rsp = InitConnect.Response.parseFrom(body);
                if (rsp.getRetType() == Common.RetType.RetType_Succeed_VALUE) {
                    synchronized (this) {
                        connID = rsp.getS2C().getConnID();
                        userID = rsp.getS2C().getLoginUserID();
                        keepAliveInterval = rsp.getS2C().getKeepAliveInterval() * 1000 * 4 / 5;
                        aesKey = rsp.getS2C().getConnAESKey();
                        aesCBIV = rsp.getS2C().getAesCBCiv();
                        cipher = new AesCbcCipher(aesKey.getBytes(StandardCharsets.UTF_8),
                                aesCBIV.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    errCode = makeInitConnectErrCode(INIT_FAIL, InitFailType.OPENDREJECT.getCode());
                    if (rsp.getRetMsg() != null && !rsp.getRetMsg().isEmpty()) {
                        desc = rsp.getRetMsg();
                    } else {
                        desc = String.format("retType=%d", rsp.getRetType());
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                errCode = makeInitConnectErrCode(INIT_FAIL, InitFailType.OPENDREJECT.getCode());
                desc = String.format("Parse packet fail, serialNO=%d", header.nSerialNo);
            }
        } else if (replyType == ReqReplyType.Timeout){
            errCode = makeInitConnectErrCode(INIT_FAIL, InitFailType.TIMEOUT.getCode());
        } else if (replyType == ReqReplyType.DisConnect) {
            errCode = makeInitConnectErrCode(INIT_FAIL, InitFailType.DISCONNECT.getCode());
        } else if (replyType == ReqReplyType.Invalid) {
            errCode = makeInitConnectErrCode(INIT_FAIL, InitFailType.UNKNOW.getCode());
            desc = String.format("Invalid packet body, serialNO=%d", header.nSerialNo);
        }
        onInitConnect(errCode, desc);
    }

    private void handlePushPacket(ProtoHeader header, byte[] body) {
        onPush(header, body);
    }

    private long makeInitConnectErrCode(int high, int low) {
        long errCode;
        errCode = (long)high << 32;
        errCode |= low;
        return errCode;
    }

    private int connErrToConnectFailType(ConnErr err) {
        switch (err) {
            case OK:
                return ConnectFailType.NONE.getCode();
            case CONNECT_FAIL:
            case CONNECT_TIMEOUT:
                return ConnectFailType.CONNECTFAILED.getCode();
            case SEND_FAIL:
                return ConnectFailType.SENDFAILED.getCode();
            case READ_FAIL:
                return ConnectFailType.RECVFAILED.getCode();
        }
        return ConnectFailType.UNKNOWN.getCode();
    }
}
