package com.futu.openapi.sample;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTAPI_Conn_Trd;
import com.futu.openapi.FTSPI_Conn;
import com.futu.openapi.FTSPI_Trd;
import com.futu.openapi.common.Config;
import com.futu.openapi.pb.*;

public class TestTrd implements FTSPI_Conn, FTSPI_Trd {
    FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    private final Object lock = new Object();

    TestTrd() {
        /*
        设置客户端信息
         */
        trd.setClientInfo("javaclient", 1);
        /*
        设置连接相关回调
         */
        trd.setConnSpi(this);
        /*
        设置交易相关回调
         */
        trd.setTrdSpi(this);
    }

    //连接OpenD
    void start() {
        trd.initConnect(Config.opendIP, (short) Config.opendPort, false);
    }

    //解锁交易
    void unlockTrade() {
        TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
                .setUnlock(true)
                .setPwdMD5(Config.unlockTradePwdMd5)
                .setSecurityFirm(Config.securityFirm.getNumber())
                .build();
        TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
        trd.unlockTrade(req);
    }

    //获取交易账号列表
    void getAccList() {
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(0).build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        trd.getAccList(req);
        System.out.println("get acc list");
    }

    //下单
    void placeOrder() {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setAccID(Config.trdAcc)
                .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                .build();
        TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                .setPacketID(trd.nextPacketID())
                .setHeader(header)
                .setTrdSide(TrdCommon.TrdSide.TrdSide_Buy_VALUE)
                .setOrderType(TrdCommon.OrderType.OrderType_Normal_VALUE)
                .setCode("01810")
                .setQty(1000)
                .setPrice(9.95)
                .setAdjustPrice(true)
                .setSecMarket(TrdCommon.TrdSecMarket.TrdSecMarket_HK_VALUE)
                .build();
        TrdPlaceOrder.Request req = TrdPlaceOrder.Request.newBuilder().setC2S(c2s).build();
        trd.placeOrder(req);
    }

    void getMarginRatio() {
        QotCommon.Security sec = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security_VALUE)
                .setCode("00388")
                .build();
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                .setAccID(Config.trdAcc)
                .build();
        TrdGetMarginRatio.C2S c2s = TrdGetMarginRatio.C2S.newBuilder()
                .setHeader(header)
                .addSecurityList(sec)
                .build();
        TrdGetMarginRatio.Request req = TrdGetMarginRatio.Request.newBuilder()
                .setC2S(c2s)
                .build();
        int sn = trd.getMarginRatio(req);
        System.out.printf("getMarginRatio: sn=%d\n", sn);
    }

    //订阅交易推送，否则不会收到订单相关通知。
    void subAccPush() {
        TrdSubAccPush.C2S c2s = TrdSubAccPush.C2S.newBuilder().addAccIDList(Config.trdAcc).build();
        TrdSubAccPush.Request req = TrdSubAccPush.Request.newBuilder().setC2S(c2s).build();
        trd.subAccPush(req);
    }

    //与OpenD连接和初始化完成，可以进行各种业务请求。如果ret为false，表示失败，desc中有错误信息
    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        System.out.printf("Trd onInitConnect: ret=%b desc=%s connID=%d\n", errCode, desc, client.getConnectID());
        if (errCode != 0)
            return;

        getAccList();
//        unlockTrade();
    }

    //连接断开
    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        System.out.printf("Trd onDisConnect: %d\n", errCode);
    }

    @Override
    public void onReply_GetAccList(FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp) {
        System.out.printf("onReply_GetAccList: %d %s\n", nSerialNo, rsp.toString());

    }

    @Override
    public void onReply_UnlockTrade(FTAPI_Conn client, int nSerialNo, TrdUnlockTrade.Response rsp) {
        System.out.printf("onReply_UnlockTrade: %d %s\n", nSerialNo, rsp.toString());
        subAccPush();
        placeOrder();
    }

    @Override
    public void onReply_SubAccPush(FTAPI_Conn client, int nSerialNo, TrdSubAccPush.Response rsp) {
        System.out.printf("onReply_SubAccPush: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_PlaceOrder(FTAPI_Conn client, int nSerialNo, TrdPlaceOrder.Response rsp) {
        System.out.printf("onReply_PlaceOrder: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetMarginRatio(FTAPI_Conn client, int nSerialNo, TrdGetMarginRatio.Response rsp) {
        System.out.printf("onReply_GetMarginRatio: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onPush_UpdateOrder(FTAPI_Conn client, TrdUpdateOrder.Response rsp) {
        System.out.printf("Push UpdateOrder: %s\n", rsp.toString());
    }

    @Override
    public void onPush_UpdateOrderFill(FTAPI_Conn client, TrdUpdateOrderFill.Response rsp) {
        System.out.printf("Push UpdateOrderFill: %s\n", rsp.toString());
    }
}