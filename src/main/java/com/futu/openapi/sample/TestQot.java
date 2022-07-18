package com.futu.openapi.sample;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTAPI_Conn_Qot;
import com.futu.openapi.FTSPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import com.futu.openapi.common.Config;
import com.futu.openapi.pb.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

//行情示例
public class TestQot implements FTSPI_Qot, FTSPI_Conn {
    FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

    public TestQot() {
        /*
        设置客户端信息
         */
        qot.setClientInfo("javaclient", 1);
        /*
        设置连接回调
         */
        qot.setConnSpi(this);
        /*
        设置行情回调
         */
        qot.setQotSpi(this);
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
