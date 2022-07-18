package com.futu.openapi.sample;

import com.futu.openapi.*;
import com.futu.openapi.common.Config;
import com.futu.openapi.common.Connection.ConnStatus;
import com.futu.openapi.common.ReqInfo;
import com.futu.openapi.pb.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DemoBase implements FTSPI_Conn, FTSPI_Qot, FTSPI_Trd {
    protected Object qotLock = new Object();
    protected Object trdLock = new Object();
    protected FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();
    protected FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    protected ConnStatus qotConnStatus = ConnStatus.DISCONNECT;
    protected ConnStatus trdConnStatus = ConnStatus.DISCONNECT;
    protected HashMap<Integer, ReqInfo> qotReqInfoMap = new HashMap<>();
    protected HashMap<Integer, ReqInfo> trdReqInfoMap = new HashMap<>();

    @Override
    public void onInitConnect(FTAPI_Conn client, long errCode, String desc) {
        if (client instanceof FTAPI_Conn_Qot) {
            synchronized (qotLock) {
                if (errCode == 0) {
                    qotConnStatus = ConnStatus.READY;
                }
                qotLock.notifyAll();
                return;
            }
        }

        if (client instanceof FTAPI_Conn_Trd) {
            synchronized (trdLock) {
                if (errCode == 0) {
                    trdConnStatus = ConnStatus.READY;
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
                qotConnStatus = ConnStatus.DISCONNECT;
                return;
            }
        }

        if (client instanceof FTAPI_Conn_Trd) {
            synchronized (trdLock) {
                trdConnStatus = ConnStatus.DISCONNECT;
            }
        }
    }

    boolean initConnectQotSync(String ip, short port) throws InterruptedException {
        qot.setConnSpi(this);
        qot.setQotSpi(this);
        synchronized (qotLock) {
            boolean ret = qot.initConnect(ip, port, false);
            if (!ret)
                return false;
            qotLock.wait();
            return qotConnStatus == ConnStatus.READY;
        }
    }

    boolean initConnectTrdSync(String ip, short port) throws InterruptedException {
        trd.setConnSpi(this);
        trd.setTrdSpi(this);
        synchronized (trdLock) {
            boolean ret = trd.initConnect(ip, port, false);
            if (!ret)
                return false;
            trdLock.wait();
            return trdConnStatus == ConnStatus.READY;
        }
    }

    QotGetSecuritySnapshot.Response getSecuritySnapshotSync(List<QotCommon.Security> secList) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != ConnStatus.READY)
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

    QotGetStaticInfo.Response getStaticInfoSync(QotGetStaticInfo.C2S c2s) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != ConnStatus.READY)
                    return null;
                QotGetStaticInfo.Request req = QotGetStaticInfo.Request.newBuilder().setC2S(c2s).build();
                int sn = qot.getStaticInfo(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.QOT_GETSTATICINFO, syncEvent);
                qotReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (QotGetStaticInfo.Response)reqInfo.rsp;
        }
    }

    QotSub.Response subSync(List<QotCommon.Security> secList,
                            List<QotCommon.SubType> subTypeList,
                            boolean isSub,
                            boolean isRegPush) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != ConnStatus.READY)
                    return null;
                QotSub.C2S c2s = QotSub.C2S.newBuilder()
                        .addAllSecurityList(secList)
                        .addAllSubTypeList(subTypeList.stream().mapToInt((QotCommon.SubType subType) -> subType.getNumber()).boxed().collect(Collectors.toList()))
                        .setIsSubOrUnSub(isSub)
                        .setIsRegOrUnRegPush(isRegPush)
                        .build();
                QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
                int sn = qot.sub(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.QOT_SUB, syncEvent);
                qotReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (QotSub.Response)reqInfo.rsp;
        }
    }

    QotGetOrderBook.Response getOrderBookSync(QotCommon.Security sec, int num) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (qotLock) {
                if (qotConnStatus != ConnStatus.READY)
                    return null;
                QotGetOrderBook.C2S c2s = QotGetOrderBook.C2S.newBuilder()
                        .setSecurity(sec)
                        .setNum(num)
                        .build();
                QotGetOrderBook.Request req = QotGetOrderBook.Request.newBuilder().setC2S(c2s).build();
                int sn = qot.getOrderBook(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.QOT_GETORDERBOOK, syncEvent);
                qotReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (QotGetOrderBook.Response)reqInfo.rsp;
        }
    }

    QotRequestHistoryKL.Response requestHistoryKLSync(QotCommon.Security sec,
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
                if (qotConnStatus != ConnStatus.READY)
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

    TrdPlaceOrder.Response placeOrderSync(TrdPlaceOrder.C2S c2s) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
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

    TrdUnlockTrade.Response unlockTradeSync(String pwdMD5, boolean isUnlock) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
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

    TrdSubAccPush.Response subTrdAccPushSync(List<Long> accList) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
                TrdSubAccPush.C2S c2s = TrdSubAccPush.C2S.newBuilder().addAllAccIDList(accList).build();
                TrdSubAccPush.Request req = TrdSubAccPush.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.subAccPush(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_SUBACCPUSH, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdSubAccPush.Response)reqInfo.rsp;
        }
    }

    TrdGetAccList.Response getAccListSync() throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
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

    TrdGetFunds.Response getFundsSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                      boolean isRefreshCache,
                                      TrdCommon.Currency currency) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
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
                reqInfo = new ReqInfo(ProtoID.TRD_GETFUNDS, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetFunds.Response)reqInfo.rsp;
        }
    }

    TrdGetOrderList.Response getOrderListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                          boolean isRefreshCache,
                                          TrdCommon.TrdFilterConditions filterConditions,
                                          List<TrdCommon.OrderStatus> filterStatusList) throws  InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetOrderList.C2S.Builder c2s = TrdGetOrderList.C2S.newBuilder()
                        .setHeader(trdHeader)
                        .setRefreshCache(isRefreshCache);
                if (filterConditions != null) {
                    c2s.setFilterConditions(filterConditions);
                }
                if (filterStatusList.size() > 0) {
                    for (TrdCommon.OrderStatus status : filterStatusList) {
                        c2s.addFilterStatusList(status.getNumber());
                    }
                }
                TrdGetOrderList.Request req = TrdGetOrderList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getOrderList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETORDERLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetOrderList.Response)reqInfo.rsp;
        }
    }

    TrdGetOrderFillList.Response getOrderFillListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                      boolean isRefreshCache,
                                                      TrdCommon.TrdFilterConditions filterConditions) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetOrderFillList.C2S.Builder c2s = TrdGetOrderFillList.C2S.newBuilder()
                        .setHeader(trdHeader)
                        .setRefreshCache(isRefreshCache);
                if (filterConditions != null) {
                    c2s.setFilterConditions(filterConditions);
                }
                TrdGetOrderFillList.Request req = TrdGetOrderFillList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getOrderFillList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETORDERFILLLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetOrderFillList.Response)reqInfo.rsp;
        }
    }

    TrdGetHistoryOrderList.Response getHistoryOrderListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                            TrdCommon.TrdFilterConditions filterConditions,
                                                            List<TrdCommon.OrderStatus> filterStatusList) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetHistoryOrderList.C2S.Builder c2s = TrdGetHistoryOrderList.C2S.newBuilder()
                        .setHeader(trdHeader);
                if (filterConditions != null) {
                    c2s.setFilterConditions(filterConditions);
                }
                if (filterStatusList.size() > 0) {
                    for (TrdCommon.OrderStatus status : filterStatusList) {
                        c2s.addFilterStatusList(status.getNumber());
                    }
                }
                TrdGetHistoryOrderList.Request req = TrdGetHistoryOrderList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getHistoryOrderList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETHISTORYORDERLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetHistoryOrderList.Response)reqInfo.rsp;
        }
    }

    TrdGetHistoryOrderFillList.Response getHistoryOrderFillListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                                    TrdCommon.TrdFilterConditions filterConditions) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
                    return null;
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdGetHistoryOrderFillList.C2S.Builder c2s = TrdGetHistoryOrderFillList.C2S.newBuilder()
                        .setHeader(trdHeader);
                if (filterConditions != null) {
                    c2s.setFilterConditions(filterConditions);
                }
                TrdGetHistoryOrderFillList.Request req = TrdGetHistoryOrderFillList.Request.newBuilder().setC2S(c2s).build();
                int sn = trd.getHistoryOrderFillList(req);
                if (sn == 0)
                    return null;
                reqInfo = new ReqInfo(ProtoID.TRD_GETHISTORYORDERFILLLIST, syncEvent);
                trdReqInfoMap.put(sn, reqInfo);
            }
            syncEvent.wait();
            return (TrdGetHistoryOrderFillList.Response)reqInfo.rsp;
        }
    }

    TrdGetPositionList.Response getPositionListSync(long accID, TrdCommon.TrdMarket trdMarket, TrdCommon.TrdEnv trdEnv,
                                                    TrdCommon.TrdFilterConditions filterConditions,
                                                    Double filterPLRatioMin,
                                                    Double filterPLRatioMax,
                                                    boolean isRefreshCache) throws InterruptedException {
        ReqInfo reqInfo = null;
        Object syncEvent = new Object();

        synchronized (syncEvent) {
            synchronized (trdLock) {
                if (trdConnStatus != ConnStatus.READY)
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

    static QotCommon.Security makeSec(QotCommon.QotMarket market, String code) {
        QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(code)
                .setMarket(market.getNumber())
                .build();
        return sec;
    }

    static TrdCommon.TrdHeader makeTrdHeader(TrdCommon.TrdEnv trdEnv,
                                             long accID,
                                             TrdCommon.TrdMarket trdMarket) {
        TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                .setTrdEnv(trdEnv.getNumber())
                .setAccID(accID)
                .setTrdMarket(trdMarket.getNumber())
                .build();
        return header;
    }
}
