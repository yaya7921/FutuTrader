package com.futu.openapi.sample;

import com.futu.openapi.pb.*;
import com.futu.openapi.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;


//行情示例
class TestQot implements FTSPI_Qot, FTSPI_Conn {
    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public TestQot() {
        qot.setClientInfo("javaclient", 1);  //设置客户端信息
        qot.setConnSpi(this);  //设置连接回调
        qot.setQotSpi(this);   //设置行情回调
    }

    //连接OpenD
    public void start(boolean isEnableEncrypt) {
        if (isEnableEncrypt) {
            String rsaKey = null;
            try {
                byte[] buf = java.nio.file.Files.readAllBytes(Paths.get(Config.rsaKeyFilePath));
                rsaKey = new String(buf, StandardCharsets.UTF_8);
                qot.setRSAPrivateKey(rsaKey);
            } catch (IOException e) {

            }
        }

        qot.initConnect(Config.opendIP, (short) Config.opendPort, isEnableEncrypt);
    }

    //获取全局状态
    public void getGlobalState() {
        GetGlobalState.Request req = GetGlobalState.Request.newBuilder().setC2S(
                GetGlobalState.C2S.newBuilder().setUserID(Config.userID)
        ).build();int seqNo = qot.getGlobalState(req);
        System.out.printf("Send GetGlobalState: %d\n", seqNo);
    }

    //测试订阅行情
    void sub() {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode("999010")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Future_VALUE)
                .build();
        QotSub.C2S c2s = QotSub.C2S.newBuilder().addSecurityList(sec)
                .addSubTypeList(QotCommon.SubType.SubType_Basic_VALUE)
                .setIsSubOrUnSub(true)
                .setIsRegOrUnRegPush(true)
                .setIsFirstPush(true)
                .build();
        QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
        qot.sub(req);
    }

    //测试获取订阅状态
    void getSubInfo() {
        QotGetSubInfo.C2S c2s = QotGetSubInfo.C2S.newBuilder().build();
        QotGetSubInfo.Request req = QotGetSubInfo.Request.newBuilder().setC2S(c2s).build();
        qot.getSubInfo(req);
    }

    //测试拉取历史K线数据
    void requestHistoryKL() {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode("TSLA")
                .setMarket(QotCommon.QotMarket.QotMarket_US_Security.getNumber())
                .build();
        QotRequestHistoryKL.C2S c2s = QotRequestHistoryKL.C2S.newBuilder()
                .setSecurity(sec)
                .setRehabType(QotCommon.RehabType.RehabType_Forward_VALUE)
                .setKlType(QotCommon.KLType.KLType_Day_VALUE)
                .setBeginTime("2021-04-28 00:00:00")
                .setEndTime("2021-04-29 16:00:00")
                .setMaxAckKLNum(1000)
                .build();
        QotRequestHistoryKL.Request req = QotRequestHistoryKL.Request.newBuilder().setC2S(c2s).build();
        qot.requestHistoryKL(req);
    }

    //测试获取基本行情，需要先订阅才能获取
    void getBasicQot() {
        QotCommon.Security sec1 = QotCommon.Security.newBuilder().setCode("00388")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .build();
        QotGetBasicQot.C2S c2s = QotGetBasicQot.C2S.newBuilder().addSecurityList(sec1).build();
        QotGetBasicQot.Request req = QotGetBasicQot.Request.newBuilder().setC2S(c2s).build();
        qot.getBasicQot(req);
    }

    //测试获取期权到期日
    void getOptionExpirationDate() {
        QotCommon.Security sec1 = QotCommon.Security.newBuilder().setCode("00388")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .build();
        QotGetOptionExpirationDate.C2S c2s = QotGetOptionExpirationDate.C2S.newBuilder()
                .setOwner(sec1)
                .build();
        QotGetOptionExpirationDate.Request req = QotGetOptionExpirationDate.Request.newBuilder()
                .setC2S(c2s)
                .build();
        qot.getOptionExpirationDate(req);
    }

    void requestTradeDate() {
        QotRequestTradeDate.C2S c2s = QotRequestTradeDate.C2S.newBuilder().setMarket(QotCommon.QotMarket.QotMarket_HK_Security_VALUE)
                .setBeginTime("2020-02-01")
                .setEndTime("2020-02-20")
                .build();
        QotRequestTradeDate.Request req = QotRequestTradeDate.Request.newBuilder().setC2S(c2s).build();
        qot.requestTradeDate(req);
    }

    void stockFilter() {
        QotStockFilter.BaseFilter baseFilter = QotStockFilter.BaseFilter.newBuilder()
                .setFieldName(QotStockFilter.StockField.StockField_MarketVal.getNumber())
                .setFilterMin(10000)
                .setFilterMax(10000000000.0)
                .setIsNoFilter(false)
                .setSortDir(QotStockFilter.SortDir.SortDir_Descend.getNumber())
                .build();
        QotStockFilter.C2S c2s = QotStockFilter.C2S.newBuilder()
                .setBegin(0)
                .setNum(100)
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .addBaseFilterList(baseFilter)
                .build();
        int serialNo = qot.stockFilter(QotStockFilter.Request.newBuilder().setC2S(c2s).build());
        System.out.printf("SendQotStockFilter: %d\n", serialNo);
    }

    void getOptionChain() {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode("00700")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .build();
        QotGetOptionChain.DataFilter dataFilter = QotGetOptionChain.DataFilter.newBuilder().setDeltaMin(0.5).setDeltaMax(0.75).build();
        QotGetOptionChain.C2S c2s = QotGetOptionChain.C2S.newBuilder().setOwner(sec)
                .setBeginTime("2020-03-01")
                .setEndTime("2020-03-31")
                .setType(QotCommon.OptionType.OptionType_Call_VALUE)
                .setDataFilter(dataFilter)
                .build();
        QotGetOptionChain.Request req = QotGetOptionChain.Request.newBuilder().setC2S(c2s).build();
        qot.getOptionChain(req);
    }

    void setPriceReminder() {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode("00700")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .build();
        QotSetPriceReminder.C2S c2s = QotSetPriceReminder.C2S.newBuilder().setSecurity(sec)
                .setOp(QotSetPriceReminder.SetPriceReminderOp.SetPriceReminderOp_Add_VALUE)
                .setType(QotCommon.PriceReminderType.PriceReminderType_PriceUp_VALUE)
                .setFreq(QotCommon.PriceReminderFreq.PriceReminderFreq_Always_VALUE)
                .setValue(380)
                .build();
        QotSetPriceReminder.Request req = QotSetPriceReminder.Request.newBuilder().setC2S(c2s).build();
        qot.setPriceReminder(req);
    }

    void getPriceReminder() {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode("00700")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security.getNumber())
                .build();
        QotGetPriceReminder.C2S c2s = QotGetPriceReminder.C2S.newBuilder().setSecurity(sec).build();
        QotGetPriceReminder.Request req = QotGetPriceReminder.Request.newBuilder().setC2S(c2s).build();
        qot.getPriceReminder(req);
    }

    void getUserSecurityGroup() {
        QotGetUserSecurityGroup.C2S c2s = QotGetUserSecurityGroup.C2S.newBuilder()
                .setGroupType(QotGetUserSecurityGroup.GroupType.GroupType_All_VALUE)
                .build();
        QotGetUserSecurityGroup.Request req = QotGetUserSecurityGroup.Request.newBuilder().setC2S(c2s).build();
        qot.getUserSecurityGroup(req);
    }

    void getMarketState() {
        QotCommon.Security sec = QotCommon.Security.newBuilder()
                .setCode("00700")
                .setMarket(QotCommon.QotMarket.QotMarket_HK_Security_VALUE)
                .build();
        QotGetMarketState.C2S c2s = QotGetMarketState.C2S.newBuilder()
                .addSecurityList(sec)
                .build();
        QotGetMarketState.Request req = QotGetMarketState.Request.newBuilder().setC2S(c2s).build();
        qot.getMarketState(req);
    }

    //与OpenD连接和初始化完成，可以进行各种业务请求。如果ret为false，表示失败，desc中有错误信息
    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        System.out.printf("Qot onInitConnect: ret=%d desc=%s connID=%d\n", errCode, desc, client.getConnectID());
        if (errCode != 0)
            return;

//        InitConnect成功返回才能继续后面的请求
//        this.getGlobalState();
//        this.sub();
//        this.getSubInfo();
//        this.requestHistoryKL();
//        this.getBasicQot();
//        this.stockFilter();
//        this.setPriceReminder();
//        this.getPriceReminder();
//        this.getUserSecurityGroup();
//        this.getMarketState();
//        this.getOptionExpirationDate();
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        System.out.printf("Qot onDisConnect: %d\n", errCode);
    }

    @Override
    public void onReply_GetGlobalState(FTAPI_Conn client, int nSerialNo, GetGlobalState.Response rsp) {
        System.out.printf("Reply: GetGlobalState: %d  %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
        System.out.printf("Reply: Sub: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetSubInfo(FTAPI_Conn client, int nSerialNo, QotGetSubInfo.Response rsp) {
        System.out.printf("Reply: getSubInfo: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetPlateSet(FTAPI_Conn client, int nSerialNo, QotGetPlateSet.Response rsp) {
        System.out.printf("Reply GetPlateSet: %d  %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_RequestHistoryKL(FTAPI_Conn client, int nSerialNo, QotRequestHistoryKL.Response rsp) {
        System.out.printf("Reply: RequestHistoryKL: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetBasicQot(FTAPI_Conn client, int nSerialNo, QotGetBasicQot.Response rsp) {
        System.out.printf("Reply: GetBasicQot: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_RequestTradeDate(FTAPI_Conn client, int nSerialNo, QotRequestTradeDate.Response rsp) {
        System.out.printf("onReply_RequestTradeDate: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_StockFilter(FTAPI_Conn client, int nSerialNo, QotStockFilter.Response rsp) {
        System.out.printf("onReply_StockFilter: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetOptionChain(FTAPI_Conn client, int nSerialNo, QotGetOptionChain.Response rsp) {
        System.out.printf("onReply_GetOptionChain: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_SetPriceReminder(FTAPI_Conn client, int nSerialNo, QotSetPriceReminder.Response rsp) {
        System.out.printf("onReply_SetPriceReminder: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onReply_GetPriceReminder(FTAPI_Conn client, int nSerialNo, QotGetPriceReminder.Response rsp) {
        System.out.printf("onReply_GetPriceReminder: %d %s\n", nSerialNo, rsp.toString());
    }

    @Override
    public void onPush_UpdateKL(FTAPI_Conn client, QotUpdateKL.Response rsp) {
        System.out.printf("onPush_UpdateKL: %s\n", rsp.toString());
    }

    @Override
    public void onPush_UpdateBasicQuote(FTAPI_Conn client, QotUpdateBasicQot.Response rsp) {
        System.out.printf("onPush_UpdateBasicQuote: %s\n", rsp.toString());
    }

    @Override
    public void onPush_UpdatePriceReminder(FTAPI_Conn client, QotUpdatePriceReminder.Response rsp) {
        System.out.printf("onPush_UpdatePriceReminder: %s\n", rsp.toString());
    }

    @Override
    public void onReply_GetUserSecurityGroup(FTAPI_Conn client, int nSerialNo, QotGetUserSecurityGroup.Response rsp) {
        System.out.printf("onReply_GetUserSecurityGroup: %s\n", rsp.toString());
    }

    @Override
    public void onReply_GetMarketState(FTAPI_Conn client, int nSerialNo, QotGetMarketState.Response rsp) {
        System.out.printf("onReply_GetMarketState: %s\n", rsp.toString());
    }

    @Override
    public void onReply_GetOptionExpirationDate(FTAPI_Conn client, int nSerialNo, QotGetOptionExpirationDate.Response rsp) {
        System.out.printf("onReply_GetOptionExpirationDate: %s\n", rsp.toString());
    }
}


class TestTrd implements FTSPI_Conn, FTSPI_Trd {
    FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    private final Object lock = new Object();

    TestTrd() {
        trd.setClientInfo("javaclient", 1);  //设置客户端信息
        trd.setConnSpi(this);  //设置连接相关回调
        trd.setTrdSpi(this);   //设置交易相关回调
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

public class Main {
    static String[] parameter = {"SecuritySnapshot", "MACDStrategy", "QuoteAndTrade", "StockSell"};
    public static void main(String[] args) throws Exception {

        FTAPI.init();

        int nDemoIndex = 0;
        if (args.length > 0) {
            for (int i = 0; i < parameter.length; i++) {
                if (args[0].equals(parameter[i])) {
                    nDemoIndex = i;
                }
            }
        }

        switch (nDemoIndex) {
            case 0: {
                GetSecuritySnapshotDemo demo = new GetSecuritySnapshotDemo();
                demo.run(QotCommon.QotMarket.QotMarket_HK_Security);
                break;
            }
            case 1: {
                MACDStrategyDemo demo = new MACDStrategyDemo();
                QotCommon.Security security = QotCommon.Security.newBuilder()
                        .setMarket(QotCommon.QotMarket.QotMarket_HK_Security_VALUE)
                        .setCode("00700")
                        .build();
                demo.run("e10adc3949ba59abbe56e057f20f883e", TrdCommon.TrdMarket.TrdMarket_HK,
                        TrdCommon.TrdEnv.TrdEnv_Simulate, security);
                break;
            }
            case 2: {
                QuoteAndTradeDemo demo = new QuoteAndTradeDemo();
                demo.quoteTest();
                break;
            }
            case 3: {
                StockSellDemo demo = new StockSellDemo();
                demo.simpleSell("00700", QotCommon.QotMarket.QotMarket_HK_Security,
                        TrdCommon.TrdSecMarket.TrdSecMarket_HK, 580.0, 100,
                        TrdCommon.OrderType.OrderType_Normal, TrdCommon.TrdEnv.TrdEnv_Simulate,
                        TrdCommon.TrdMarket.TrdMarket_HK, Config.unlockTradePwdMd5);
                break;
            }
            default:{
                break;
            }
        }

        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException exc) {
            System.out.println("sleep error");
        }
        FTAPI.unInit();
    }
}
