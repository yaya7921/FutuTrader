package com.panini.fututrade.base;

import com.futu.openapi.*;
import com.futu.openapi.common.Config;
import com.futu.openapi.common.Connection;
import com.futu.openapi.common.ReqInfo;
import com.futu.openapi.pb.*;
import com.google.protobuf.ByteString;

import java.util.Arrays;
import java.util.List;

/**
 * @author shuyun
 */
public class FutuBase implements FTSPI_Conn, FTSPI_Qot, FTSPI_Trd {
    protected FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();
    protected FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    protected Connection.ConnStatus qotStatus = Connection.ConnStatus.DISCONNECT;
    protected Connection.ConnStatus trdStatus = Connection.ConnStatus.DISCONNECT;

    public boolean initConnectQot(String ip, short port) {
        qot.setConnSpi(this);
        qot.setQotSpi(this);
        return qot.initConnect(ip, port, false);
    }

    public boolean initConnectTrd(String ip, short port) {
        trd.setConnSpi(this);
        trd.setTrdSpi(this);
        return trd.initConnect(ip, port, false);
    }

    public void unlockTrade() {
        TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
                .setUnlock(true)
                .setPwdMD5(Config.unlockTradePwdMd5)
                .setSecurityFirm(Config.securityFirm.getNumber())
                .build();
        TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
        trd.unlockTrade(req);
    }

    public TrdGetAccList.Response getAccList() {
        ReqInfo reqInfo;
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(Config.userID).build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.getAccList(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_GETACCLIST, new Object());
        return (TrdGetAccList.Response) reqInfo.rsp;
    }

    public TrdUnlockTrade.Response unlockTrade(String pwdMD5, boolean unlock) {
        ReqInfo reqInfo;
        TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
                .setPwdMD5(pwdMD5)
                .setUnlock(unlock)
                .setSecurityFirm(Config.securityFirm.getNumber())
                .build();
        TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.unlockTrade(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_UNLOCKTRADE, new Object());
        return (TrdUnlockTrade.Response) reqInfo.rsp;
    }

    public long getAccId(TrdCommon.TrdMarket trdMarket) {
        TrdGetAccList.Response arrAccList = getAccList();
        TrdGetAccList.S2C s2c = arrAccList.getS2C();
        int nLength = s2c.getAccListCount();
        for (int i = 0; i < nLength; i++) {
            if (s2c.getAccList(i).getTrdEnv() == TrdCommon.TrdEnv.TrdEnv_Real_VALUE &&
                    s2c.getAccList(i).getTrdMarketAuthList(0) == trdMarket.getNumber()) {
                return s2c.getAccList(i).getAccID();
            }
        }
        return 0;
    }

    public long getAccIdSim(TrdCommon.TrdMarket trdMarket,
                            TrdCommon.SimAccType simAccType) {
        TrdGetAccList.Response arrAccList = getAccList();
        TrdGetAccList.S2C s2c = arrAccList.getS2C();
        int nLength = s2c.getAccListCount();
        for (int i = 0; i < nLength; i++) {
            if (s2c.getAccList(i).getTrdEnv() == TrdCommon.TrdEnv.TrdEnv_Simulate_VALUE &&
                    s2c.getAccList(i).getTrdMarketAuthList(0) == trdMarket.getNumber()) {
                if (s2c.getAccList(i).getSimAccType() == simAccType.getNumber()) {
                    return s2c.getAccList(i).getAccID();
                }
            }
        }
        return 0;
    }

    public QotRequestHistoryKL.Response requestHistoryKL(QotCommon.Security sec,
                                                      QotCommon.KLType klType,
                                                      QotCommon.RehabType rehabType,
                                                      String beginTime,
                                                      String endTime,
                                                      Integer count,
                                                      Long klFields,
                                                      byte[] nextReqKey,
                                                      boolean extendedTime) {
        ReqInfo reqInfo;
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
        reqInfo = new ReqInfo(ProtoID.QOT_REQUESTHISTORYKL, new Object());
        return (QotRequestHistoryKL.Response)reqInfo.rsp;
    }

    public TrdGetPositionList.Response getPositionList(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                    TrdCommon.TrdFilterConditions filterConditions,
                                                    Double filterPLRatioMin,
                                                    Double filterPLRatioMax,
                                                    boolean isRefreshCache) {
        ReqInfo reqInfo;

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
        reqInfo = new ReqInfo(ProtoID.TRD_GETPOSITIONLIST, new Object());

        return (TrdGetPositionList.Response)reqInfo.rsp;
    }

    private static TrdCommon.TrdHeader makeTrdHeader(TrdCommon.TrdEnv trdEnv,
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
              TrdCommon.TrdEnv trdEnv) {
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
             TrdCommon.TrdEnv trdEnv) {
        TrdCommon.TrdSecMarket secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_Unknown;
        if (sec.getMarket() == QotCommon.QotMarket.QotMarket_HK_Security_VALUE) {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_HK;
        } else {
            secMarket = TrdCommon.TrdSecMarket.TrdSecMarket_US;
        }

        TrdGetFunds.Response getFundsRsp = getFundsSync(trdAcc, trdMarket, trdEnv, false, TrdCommon.Currency.Currency_Unknown);
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

    public QotGetSecuritySnapshot.Response getSecuritySnapshotSync(List<QotCommon.Security> secList) {
        ReqInfo reqInfo;
        QotGetSecuritySnapshot.C2S c2s = QotGetSecuritySnapshot.C2S.newBuilder()
                .addAllSecurityList(secList)
                .build();
        QotGetSecuritySnapshot.Request req = QotGetSecuritySnapshot.Request.newBuilder().setC2S(c2s).build();
        int sn = qot.getSecuritySnapshot(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.QOT_GETSECURITYSNAPSHOT, new Object());
        return (QotGetSecuritySnapshot.Response)reqInfo.rsp;
    }

    public TrdPlaceOrder.Response placeOrderSync(TrdPlaceOrder.C2S c2s) {
        ReqInfo reqInfo;
        TrdPlaceOrder.Request req = TrdPlaceOrder.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.placeOrder(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_PLACEORDER, new Object());
        return (TrdPlaceOrder.Response)reqInfo.rsp;
    }

    public TrdGetFunds.Response getFundsSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                      boolean isRefreshCache, TrdCommon.Currency currency) {
        ReqInfo reqInfo;
        TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
        TrdGetFunds.C2S c2s = TrdGetFunds.C2S.newBuilder()
                .setHeader(trdHeader)
                .setCurrency(currency.getNumber())
                .setRefreshCache(isRefreshCache)
                .build();
        TrdGetFunds.Request req = TrdGetFunds.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.getFunds(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_GETFUNDS, new Object());
        return (TrdGetFunds.Response)reqInfo.rsp;
    }
}
