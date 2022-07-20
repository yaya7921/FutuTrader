package com.panini.fututrade.base;

import com.futu.openapi.*;
import com.futu.openapi.common.Config;
import com.futu.openapi.common.Connection;
import com.futu.openapi.common.ReqInfo;
import com.futu.openapi.pb.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author shuyun
 */
public class FutuBase implements FTSPI_Conn, FTSPI_Qot, FTSPI_Trd {
    protected Object qotLock = new Object();
    protected Object trdLock = new Object();
    protected FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();
    protected FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    protected Connection.ConnStatus qotConnStatus = Connection.ConnStatus.DISCONNECT;
    protected Connection.ConnStatus trdConnStatus = Connection.ConnStatus.DISCONNECT;
    protected HashMap<Integer, ReqInfo> qotReqInfoMap = new HashMap<>();
    protected HashMap<Integer, ReqInfo> trdReqInfoMap = new HashMap<>();

    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        if (client instanceof FTAPI_Conn_Qot) {
            synchronized (qotLock) {
                if (errCode == 0) {
                    qotConnStatus = Connection.ConnStatus.READY;
                }
                qotLock.notifyAll();
                return;
            }
        }

        if (client instanceof FTAPI_Conn_Trd) {
            synchronized (trdLock) {
                if (errCode == 0) {
                    trdConnStatus = Connection.ConnStatus.READY;
                }
                trdLock.notifyAll();
                return;
            }
        }
    }

    @Override
    public void onDisconnect(FTAPI_Conn client, long errCode) {
        if (client instanceof FTAPI_Conn_Qot) {
            synchronized (qotLock) {
                qotConnStatus = Connection.ConnStatus.DISCONNECT;
                return;
            }
        }

        if (client instanceof FTAPI_Conn_Trd) {
            synchronized (trdLock) {
                trdConnStatus = Connection.ConnStatus.DISCONNECT;
            }
        }
    }

    protected boolean initConnectQotSync(String ip, short port) throws InterruptedException {
        qot.setConnSpi(this);
        qot.setQotSpi(this);
        synchronized (qotLock) {
            boolean ret = qot.initConnect(ip, port, false);
            if (!ret)
                return false;
            qotLock.wait();
            return qotConnStatus == Connection.ConnStatus.READY;
        }
    }

    protected boolean initConnectTrdSync(String ip, short port) throws InterruptedException {
        trd.setConnSpi(this);
        trd.setTrdSpi(this);
        synchronized (trdLock) {
            boolean ret = trd.initConnect(ip, port, false);
            if (!ret)
                return false;
            trdLock.wait();
            return trdConnStatus == Connection.ConnStatus.READY;
        }
    }

    protected TrdUnlockTrade.Response unlockTradeSync(String pwdMD5, boolean isUnlock) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != Connection.ConnStatus.READY)
                    return null;
                TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
                        .setPwdMD5(pwdMD5)
                        .setUnlock(isUnlock)
                        .setSecurityFirm(Config.securityFirm.getNumber())
                        .build();
                TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.unlockTrade(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_UNLOCKTRADE, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdUnlockTrade.Response)reqInfo.rsp;
        }
    }

    protected TrdGetAccList.Response getAccListSync() throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != Connection.ConnStatus.READY)
                    return null;
                TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(Config.userID).build();
                TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getAccList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETACCLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetAccList.Response)reqInfo.rsp;
        }
    }

    protected long findAccNumber(TrdGetAccList.S2C s2c, TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket,
                              TrdCommon.SimAccType simAccType) {
        int nLength = s2c.getAccListCount();
        for (int i = 0; i < nLength; i++) {
            if (s2c.getAccList(i).getTrdEnv() == trdEnv.getNumber() &&
                    s2c.getAccList(i).getTrdMarketAuthList(0) == trdMarket.getNumber()) {
                if (trdEnv == TrdCommon.TrdEnv.TrdEnv_Simulate) {
                    if (s2c.getAccList(i).getSimAccType() == simAccType.getNumber()) {
                        return s2c.getAccList(i).getAccID();
                    }
                }
                else {
                    return s2c.getAccList(i).getAccID();
                }
            }
        }
        return 0;
    }

    protected QotRequestHistoryKL.Response requestHistoryKLSync(QotCommon.Security sec,
                                                             QotCommon.KLType klType,
                                                             QotCommon.RehabType rehabType,
                                                             String beginTime,
                                                             String endTime,
                                                             Integer count,
                                                             Long klFields,
                                                             byte[] nextReqKey,
                                                             boolean extendedTime) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != Connection.ConnStatus.READY)
                    return null;
                QotRequestHistoryKL.C2S.Builder c2s = QotRequestHistoryKL.C2S.newBuilder()
                        .setSecurity(sec)
                        .setKlType(klType.getNumber())
                        .setRehabType(rehabType.getNumber())
                        .setBeginTime(beginTime)
                        .setEndTime(endTime)
                        .setExtendedTime(extendedTime);
                if (count != null) {
                    c2s.setMaxAckKLNum(count);
                }
                if (klFields != null) {
                    c2s.setNeedKLFieldsFlag(klFields);
                }
                if (nextReqKey.length > 0) {
                    c2s.setNextReqKey(ByteString.copyFrom(nextReqKey));
                }
                QotRequestHistoryKL.Request req = QotRequestHistoryKL.Request.newBuilder().setC2S(c2s).build();
                int sn = qot.requestHistoryKL(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.QOT_REQUESTHISTORYKL, syncEvent);
                qotReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (QotRequestHistoryKL.Response)reqInfo.rsp;
        }

    }

    public TrdGetPositionList.Response getPositionListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                           TrdCommon.TrdFilterConditions filterConditions,
                                                           Double filterPLRatioMin,
                                                           Double filterPLRatioMax,
                                                           boolean isRefreshCache) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != Connection.ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetPositionList.C2S.Builder c2s = TrdGetPositionList.C2S.newBuilder()
                        .setHeader(trdHeader);
                if (filterConditions != null) {
                    c2s.setFilterConditions(filterConditions);
                }
                if (filterPLRatioMin != null) {
                    c2s.setFilterPLRatioMin(filterPLRatioMin);
                }
                if (filterPLRatioMax != null) {
                    c2s.setFilterPLRatioMax(filterPLRatioMax);
                }
                c2s.setRefreshCache(isRefreshCache);
                TrdGetPositionList.Request req = TrdGetPositionList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getPositionList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETPOSITIONLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetPositionList.Response)reqInfo.rsp;
        }

    }

    protected static TrdCommon.TrdHeader makeTrdHeader(TrdCommon.TrdEnv trdEnv,
                                                     long accID,
                                                     TrdCommon.TrdMarket trdMarket) {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setTrdEnv(trdEnv.getNumber())
                .setAccID(accID)
                .setTrdMarket(trdMarket.getNumber())
                .build();
        return header;
    }

    public void sell(QotCommon.Security sec, TrdCommon.Position pstn, long trdAcc, TrdCommon.TrdMarket trdMarket,
                     TrdCommon.TrdEnv trdEnv) throws InterruptedException {
        QotGetSecuritySnapshot.Response snapshotRsp = getSecuritySnapshotSync(Arrays.asList(sec));
        if (snapshotRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
            System.err.printf("getSecuritySnapshotSync err; retType=%s msg=%s\n", snapshotRsp.getRetType(), snapshotRsp.getRetMsg());
            return;
        }
        double price = snapshotRsp.getS2C().getSnapshotList(0).getBasic().getCurPrice();
        TrdCommon.TrdSecMarket secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_Unknown;
        if (sec.getMarket() == QotCommon.QotMarket.QotMarket_HK_Security_VALUE) {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_HK;
        } else {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_US;
        }
        TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, trdAcc, trdMarket);
        TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                .setPacketID(trd.nextPacketID())
                .setHeader(trdHeader)
                .setTrdSide(TrdCommon.TrdSide.TrdSide_Sell_VALUE)
                .setOrderType(TrdCommon.OrderType.OrderType_Normal_VALUE)
                .setCode(sec.getCode())
                .setQty(pstn.getCanSellQty())
                .setPrice(price)
                .setAdjustPrice(true)
                .setSecMarket(secMarket.getNumber())
                .build();
        TrdPlaceOrder.Response placeOrderRsp = placeOrderSync(c2s);
        if (placeOrderRsp.getRetType() < 0) {
            System.out.printf("place order failed: %s\n", placeOrderRsp.getRetMsg());
        }
        else {
            System.out.printf("place order succeed, order id: %d\n", placeOrderRsp.getS2C().getOrderID());
        }
    }

    public void buy(QotCommon.Security sec, long trdAcc, TrdCommon.TrdMarket trdMarket,
                    TrdCommon.TrdEnv trdEnv) throws  InterruptedException {
        TrdCommon.TrdSecMarket secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_Unknown;
        if (sec.getMarket() == QotCommon.QotMarket.QotMarket_HK_Security_VALUE) {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_HK;
        } else {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_US;
        }

        TrdGetFunds.Response getFundsRsp = getFundsSync(trdAcc, trdMarket, trdEnv, false, TrdCommon.Currency.Currency_Unknown);
        System.out.println(getFundsRsp);
        if (getFundsRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
            System.err.printf("getFundsSync err; retType=%s msg=%s\n", getFundsRsp.getRetType(), getFundsRsp.getRetMsg());
            return;
        }
        QotGetSecuritySnapshot.Response snapshotRsp = getSecuritySnapshotSync(Arrays.asList(sec));
        if (snapshotRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
            System.err.printf("getSecuritySnapshotSync err; retType=%s msg=%s\n", snapshotRsp.getRetType(), snapshotRsp.getRetMsg());
            return;
        }
        int lotSize = snapshotRsp.getS2C().getSnapshotList(0).getBasic().getLotSize();
        double curPrice = snapshotRsp.getS2C().getSnapshotList(0).getBasic().getCurPrice();
        double cash = getFundsRsp.getS2C().getFunds().getCash();
        int qty = (int)Math.floor(cash / curPrice);
        qty = qty / lotSize * lotSize;
        TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, trdAcc, trdMarket);
        TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                .setPacketID(trd.nextPacketID())
                .setHeader(trdHeader)
                .setTrdSide(TrdCommon.TrdSide.TrdSide_Buy_VALUE)
                .setOrderType(TrdCommon.OrderType.OrderType_Normal_VALUE)
                .setCode(sec.getCode())
                .setQty(qty)
                .setPrice(curPrice)
                .setAdjustPrice(true)
                .setSecMarket(secMarket.getNumber())
                .build();
        TrdPlaceOrder.Response placeOrderRsp = placeOrderSync(c2s);
        if (placeOrderRsp.getRetType() < 0) {
            System.out.printf("place order failed: %s\n", placeOrderRsp.getRetMsg());
        }
        else {
            System.out.printf("place order succeed, order id: %d\n", placeOrderRsp.getS2C().getOrderID());
        }
    }

    protected QotGetSecuritySnapshot.Response getSecuritySnapshotSync(List<QotCommon.Security> secList) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != Connection.ConnStatus.READY)
                    return null;
                QotGetSecuritySnapshot.C2S c2s = QotGetSecuritySnapshot.C2S.newBuilder()
                        .addAllSecurityList(secList)
                        .build();
                QotGetSecuritySnapshot.Request req = QotGetSecuritySnapshot.Request.newBuilder().setC2S(c2s).build();
                int sn = qot.getSecuritySnapshot(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.QOT_GETSECURITYSNAPSHOT, syncEvent);
                qotReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (QotGetSecuritySnapshot.Response)reqInfo.rsp;
        }
    }

    protected TrdPlaceOrder.Response placeOrderSync(TrdPlaceOrder.C2S c2s) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != Connection.ConnStatus.READY)
                    return null;
                TrdPlaceOrder.Request req = TrdPlaceOrder.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.placeOrder(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_PLACEORDER, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdPlaceOrder.Response)reqInfo.rsp;
        }
    }

    protected TrdGetFunds.Response getFundsSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                             boolean isRefreshCache,
                                             TrdCommon.Currency currency) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                System.out.printf("trdConnStatus: %s", trdConnStatus);
                if (trdConnStatus != Connection.ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetFunds.C2S c2s = TrdGetFunds.C2S.newBuilder()
                        .setHeader(trdHeader)
                        .setCurrency(currency.getNumber())
                        .setRefreshCache(isRefreshCache)
                        .build();
                TrdGetFunds.Request req = TrdGetFunds.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getFunds(req);
                System.out.printf("sn=%d", sn);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETFUNDS, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetFunds.Response)reqInfo.rsp;
        }
    }

    @Override
    public void onReply_GetSecuritySnapshot(FTAPI_Conn client, int nSerialNo, QotGetSecuritySnapshot.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_GETSECURITYSNAPSHOT, rsp);
    }

    @Override
    public void onReply_GetStaticInfo(FTAPI_Conn client, int nSerialNo, QotGetStaticInfo.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_GETSTATICINFO, rsp);
    }

    @Override
    public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_SUB, rsp);
    }

    @Override
    public void onReply_GetOrderBook(FTAPI_Conn client, int nSerialNo, QotGetOrderBook.Response rsp) {
        handleQotOnReply(nSerialNo, ProtoID.QOT_GETORDERBOOK, rsp);
    }

    @Override
    public void onReply_RequestHistoryKL(FTAPI_Conn client, int nSerialNo, QotRequestHistoryKL.Response rsp) {
        System.out.println("\nonReply_RequestHistoryKL");
        handleQotOnReply(nSerialNo, ProtoID.QOT_REQUESTHISTORYKL, rsp);
    }

    @Override
    public void onReply_PlaceOrder(FTAPI_Conn client, int nSerialNo, TrdPlaceOrder.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_PLACEORDER, rsp);
    }

    @Override
    public void onReply_UnlockTrade(FTAPI_Conn client, int nSerialNo, TrdUnlockTrade.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_UNLOCKTRADE, rsp);
    }

    @Override
    public void onReply_SubAccPush(FTAPI_Conn client, int nSerialNo, TrdSubAccPush.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_SUBACCPUSH, rsp);
    }

    @Override
    public void onReply_GetAccList(FTAPI_Conn client, int nSerialNo, TrdGetAccList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETACCLIST, rsp);
    }

    @Override
    public void onReply_GetFunds(FTAPI_Conn client, int nSerialNo, TrdGetFunds.Response rsp) {
        System.out.println("\nonReply_GetFunds");
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETFUNDS, rsp);
    }

    @Override
    public void onReply_GetOrderList(FTAPI_Conn client, int nSerialNo, TrdGetOrderList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETORDERLIST, rsp);
    }

    @Override
    public void onReply_GetOrderFillList(FTAPI_Conn client, int nSerialNo, TrdGetOrderFillList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETORDERFILLLIST, rsp);
    }

    @Override
    public void onReply_GetHistoryOrderList(FTAPI_Conn client, int nSerialNo, TrdGetHistoryOrderList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETHISTORYORDERLIST, rsp);
    }

    @Override
    public void onReply_GetHistoryOrderFillList(FTAPI_Conn client, int nSerialNo, TrdGetHistoryOrderFillList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETHISTORYORDERFILLLIST, rsp);
    }

    @Override
    public void onReply_GetPositionList(FTAPI_Conn client, int nSerialNo, TrdGetPositionList.Response rsp) {
        handleTrdOnReply(nSerialNo, ProtoID.TRD_GETPOSITIONLIST, rsp);
    }

    ReqInfo getQotReqInfo(int serialNo, int protoID) {
        synchronized (qotLock) {
            ReqInfo info = qotReqInfoMap.getOrDefault(serialNo, null);
            if (info != null && info.protoID == protoID) {
                qotReqInfoMap.remove(serialNo);
                return info;
            }
        }
        return null;
    }

    ReqInfo getTrdReqInfo(int serialNo, int protoID) {
        synchronized (trdLock) {
            ReqInfo info = trdReqInfoMap.getOrDefault(serialNo, null);
            if (info != null && info.protoID == protoID) {
                trdReqInfoMap.remove(serialNo);
                return info;
            }
        }
        return null;
    }

    void handleQotOnReply(int serialNo, int protoID, GeneratedMessageV3 rsp) {
        ReqInfo reqInfo = getQotReqInfo(serialNo, protoID);
        if (reqInfo != null) {
            synchronized (reqInfo.syncEvent) {
                reqInfo.rsp = rsp;
                reqInfo.syncEvent.notifyAll();
            }
        }
    }

    void handleTrdOnReply(int serialNo, int protoID, GeneratedMessageV3 rsp) {
        ReqInfo reqInfo = getTrdReqInfo(serialNo, protoID);
        if (reqInfo != null) {
            synchronized (reqInfo.syncEvent) {
                reqInfo.rsp = rsp;
                reqInfo.syncEvent.notifyAll();
            }
        }
    }
}
