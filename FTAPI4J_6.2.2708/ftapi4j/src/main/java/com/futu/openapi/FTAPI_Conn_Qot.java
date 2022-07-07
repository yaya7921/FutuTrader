package com.futu.openapi;

import com.google.protobuf.InvalidProtocolBufferException;
import com.futu.openapi.pb.*;

public class FTAPI_Conn_Qot extends FTAPI_Conn {
    private FTSPI_Qot qotSpi;
    final private Object qotSpiLock = new Object();

    /***
     * 设置行情请求的回调
     * @param callback
     */
    public void setQotSpi(FTSPI_Qot callback) {
        synchronized (qotSpiLock) {
            this.qotSpi = callback;
        }
    }

    /***
     * 获取全局状态，具体字段请参考GetGlobalState.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getGlobalState(GetGlobalState.Request req) {
        return sendProto(ProtoID.GETGLOBALSTATE, req);
    }

    /***
     * 订阅或者反订阅，具体字段请参考Qot_Sub.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int sub(QotSub.Request req) {
        return sendProto(ProtoID.QOT_SUB, req);
    }

    /***
     * 注册推送，具体字段请参考Qot_RegQotPush.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int regQotPush(QotRegQotPush.Request req) {
        return sendProto(ProtoID.QOT_REGQOTPUSH, req);
    }

    /***
     * 获取订阅信息，具体字段请参考Qot_GetSubInfo.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getSubInfo(QotGetSubInfo.Request req) {
        return sendProto(ProtoID.QOT_GETSUBINFO, req);
    }

    /***
     * 获取逐笔,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Ticker)，具体字段请参考Qot_GetTicker.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getTicker(QotGetTicker.Request req) {
        return sendProto(ProtoID.QOT_GETTICKER, req);
    }

    /***
     * 获取基本行情,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Basic)，具体字段请参考Qot_GetBasicQot.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getBasicQot(QotGetBasicQot.Request req) {
        return sendProto(ProtoID.QOT_GETBASICQOT, req);
    }

    /***
     * 获取摆盘,调用该接口前需要先订阅(订阅位：Qot_Common.SubType_OrderBook)，具体字段请参考Qot_GetOrderBook.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getOrderBook(QotGetOrderBook.Request req) {
        return sendProto(ProtoID.QOT_GETORDERBOOK, req);
    }

    /***
     * 获取K线，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_KL_XXX)，具体字段请参考Qot_GetKL.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getKL(QotGetKL.Request req) {
        return sendProto(ProtoID.QOT_GETKL, req);
    }

    /***
     * 获取分时，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_RT)，具体字段请参考Qot_GetRT.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getRT(QotGetRT.Request req) {
        return sendProto(ProtoID.QOT_GETRT, req);
    }

    /***
     * 获取经纪队列，调用该接口前需要先订阅(订阅位：Qot_Common.SubType_Broker)，具体字段请参考Qot_GetBroker.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getBroker(QotGetBroker.Request req) {
        return sendProto(ProtoID.QOT_GETBROKER, req);
    }

    /***
     * 在线请求历史复权信息，不读本地历史数据DB，具体字段请参考Qot_RequestRehab.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int requestRehab(QotRequestRehab.Request req) {
        return sendProto(ProtoID.QOT_REQUESTREHAB, req);
    }

    /***
     * 在线请求历史K线，不读本地历史数据DB，具体字段请参考Qot_RequestHistoryKL.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int requestHistoryKL(QotRequestHistoryKL.Request req) {
        return sendProto(ProtoID.QOT_REQUESTHISTORYKL, req);
    }

    /***
     * 获取历史K线已经用掉的额度，具体字段请参考Qot_RequestHistoryKLQuota.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int requestHistoryKLQuota(QotRequestHistoryKLQuota.Request req) {
        return sendProto(ProtoID.QOT_REQUESTHISTORYKLQUOTA, req);
    }

    /***
     * 获取静态信息，具体字段请参考Qot_GetStaticInfo.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getStaticInfo(QotGetStaticInfo.Request req) {
        return sendProto(ProtoID.QOT_GETSTATICINFO, req);
    }

    /***
     * 获取股票快照，具体字段请参考Qot_GetSecuritySnapshot.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getSecuritySnapshot(QotGetSecuritySnapshot.Request req) {
        return sendProto(ProtoID.QOT_GETSECURITYSNAPSHOT, req);
    }

    /***
     * 获取板块集合下的板块，具体字段请参考Qot_GetPlateSet.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getPlateSet(QotGetPlateSet.Request req) {
        return sendProto(ProtoID.QOT_GETPLATESET, req);
    }

    /***
     * 获取板块下的股票，具体字段请参考Qot_GetPlateSecurity.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getPlateSecurity(QotGetPlateSecurity.Request req) {
        return sendProto(ProtoID.QOT_GETPLATESECURITY, req);
    }

    /***
     * 获取相关股票，具体字段请参考Qot_GetReference.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getReference(QotGetReference.Request req) {
        return sendProto(ProtoID.QOT_GETREFERENCE, req);
    }

    /***
     * 获取股票所属的板块，具体字段请参考Qot_GetOwnerPlate.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getOwnerPlate(QotGetOwnerPlate.Request req) {
        return sendProto(ProtoID.QOT_GETOWNERPLATE, req);
    }

    /***
     * 获取大股东持股变化列表，具体字段请参考Qot_GetHoldingChangeList.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getHoldingChangeList(QotGetHoldingChangeList.Request req) {
        return sendProto(ProtoID.QOT_GETHOLDINGCHANGELIST, req);
    }

    /***
     * 筛选期权，具体字段请参考Qot_GetOptionChain.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getOptionChain(QotGetOptionChain.Request req) {
        return sendProto(ProtoID.QOT_GETOPTIONCHAIN, req);
    }

    /**
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getOptionExpirationDate(QotGetOptionExpirationDate.Request req) {
        return sendProto(ProtoID.QOT_GETOPTIONEXPIRATIONDATE, req);
    }

    /***
     * 筛选窝轮，具体字段请参考Qot_GetWarrant.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getWarrant(QotGetWarrant.Request req) {
        return sendProto(ProtoID.QOT_GETWARRANT, req);
    }

    /***
     * 获取资金流向，具体字段请参考Qot_GetCapitalFlow.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getCapitalFlow(QotGetCapitalFlow.Request req) {
        return sendProto(ProtoID.QOT_GETCAPITALFLOW, req);
    }

    /***
     * 获取资金分布，具体字段请参考Qot_GetCapitalDistribution.proto协议
     * @param req 请求参数
     * @return 请求的序列号
     */
    public int getCapitalDistribution(QotGetCapitalDistribution.Request req) {
        return sendProto(ProtoID.QOT_GETCAPITALDISTRIBUTION, req);
    }

    /***
     * 获取自选股分组下的股票，具体字段请参考Qot_GetUserSecurity.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getUserSecurity(QotGetUserSecurity.Request req) {
        return sendProto(ProtoID.QOT_GETUSERSECURITY, req);
    }

    /***
     * 修改自选股分组下的股票，具体字段请参考Qot_ModifyUserSecurity.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int modifyUserSecurity(QotModifyUserSecurity.Request req) {
        return sendProto(ProtoID.QOT_MODIFYUSERSECURITY, req);
    }

    /***
     * 获取自选股分组，具体字段请参考Qot_GetUserSecurityGroup.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getUserSecurityGroup(QotGetUserSecurityGroup.Request req) {
        return sendProto(ProtoID.QOT_GETUSERSECURITYGROUP, req);
    }

    /***
     * 条件选股，具体字段请参考Qot_StockFilter.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int stockFilter(QotStockFilter.Request req) {
        return sendProto(ProtoID.QOT_STOCKFILTER, req);
    }

    /***
     * 获取股票代码变化信息，具体字段请参考Qot_GetCodeChange.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getCodeChange(QotGetCodeChange.Request req) {
        return sendProto(ProtoID.QOT_GETCODECHANGE, req);
    }
    /***
     * 获取IPO列表，具体字段请参考Qot_GetIpoList.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getIpoList(QotGetIpoList.Request req) { return sendProto(ProtoID.QOT_GETIPOLIST, req); }
    /***
     * 获取获取期货合约资料, 具体字段请参考Qot_GetFutureInfo.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getFutureInfo(QotGetFutureInfo.Request req) { return sendProto(ProtoID.QOT_GETFUTUREINFO, req); }
    /***
     * 在线拉取交易日, 具体字段请参考QotRequestTradeDate.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int requestTradeDate(QotRequestTradeDate.Request req) { return sendProto(ProtoID.QOT_REQUESTTRADEDATE, req); }
    /***
     * 设置到价提醒, 具体字段请参考QotSetPriceReminder.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int setPriceReminder(QotSetPriceReminder.Request req) { return sendProto(ProtoID.QOT_SETPRICEREMINDER, req); }

    /***
     * 获取到价提醒, 具体字段请参考QotGetPriceReminder.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getPriceReminder(QotGetPriceReminder.Request req) { return sendProto(ProtoID.QOT_GETPRICEREMINDER, req); }

    /***
     * 获取到价提醒, 具体字段请参考QotGetMarketState.proto协议
     * @param req
     * @return 请求的序列号
     */
    public int getMarketState(QotGetMarketState.Request req) { return sendProto(ProtoID.QOT_GETMARKETSTATE, req); }

    @Override
    protected void onReply(ReqReplyType replyType, ProtoHeader protoHeader, byte[] data) {
        int protoID = protoHeader.nProtoID;
        int serialNo = protoHeader.nSerialNo;
        FTSPI_Qot qotSpi = null;
        synchronized (qotSpiLock) {
            if (this.qotSpi == null) {
                return;
            }
            qotSpi = this.qotSpi;
        }

        switch (protoID) {
            case ProtoID.GETGLOBALSTATE://获取全局状态
            {
                GetGlobalState.Response rsp = null;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = GetGlobalState.Response.parseFrom(data);

                    } catch (InvalidProtocolBufferException e) {
                        rsp = GetGlobalState.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = GetGlobalState.Response.newBuilder().setRetType(replyType.getCode()).build();
                }
                qotSpi.onReply_GetGlobalState(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETBASICQOT: {
                QotGetBasicQot.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetBasicQot.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetBasicQot.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetBasicQot.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetBasicQot(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETBROKER: {
                QotGetBroker.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetBroker.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetBroker.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetBroker.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetBroker(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETCAPITALDISTRIBUTION: {
                QotGetCapitalDistribution.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetCapitalDistribution.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetCapitalDistribution.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetCapitalDistribution.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetCapitalDistribution(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETCAPITALFLOW: {
                QotGetCapitalFlow.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetCapitalFlow.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetCapitalFlow.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetCapitalFlow.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetCapitalFlow(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETCODECHANGE: {
                QotGetCodeChange.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetCodeChange.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetCodeChange.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetCodeChange.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetCodeChange(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETHOLDINGCHANGELIST: {
                QotGetHoldingChangeList.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetHoldingChangeList.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetHoldingChangeList.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetHoldingChangeList.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetHoldingChangeList(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETKL: {
                QotGetKL.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetKL.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetKL.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetKL.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetKL(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETOPTIONCHAIN: {
                QotGetOptionChain.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetOptionChain.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetOptionChain.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetOptionChain.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetOptionChain(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETOPTIONEXPIRATIONDATE: {
                QotGetOptionExpirationDate.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetOptionExpirationDate.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetOptionExpirationDate.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetOptionExpirationDate.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetOptionExpirationDate(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETORDERBOOK: {
                QotGetOrderBook.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetOrderBook.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetOrderBook.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetOrderBook.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetOrderBook(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETOWNERPLATE: {
                QotGetOwnerPlate.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetOwnerPlate.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetOwnerPlate.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetOwnerPlate.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetOwnerPlate(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETPLATESECURITY: {
                QotGetPlateSecurity.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetPlateSecurity.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetPlateSecurity.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetPlateSecurity.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetPlateSecurity(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETPLATESET: {
                QotGetPlateSet.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetPlateSet.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetPlateSet.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetPlateSet.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetPlateSet(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETREFERENCE: {
                QotGetReference.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetReference.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetReference.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetReference.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetReference(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETREHAB: {
                QotGetRehab.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetRehab.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetRehab.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetRehab.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetRehab(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETRT: {
                QotGetRT.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetRT.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetRT.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetRT.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetRT(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETSECURITYSNAPSHOT: {
                QotGetSecuritySnapshot.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetSecuritySnapshot.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetSecuritySnapshot.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetSecuritySnapshot.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetSecuritySnapshot(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETSTATICINFO: {
                QotGetStaticInfo.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetStaticInfo.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetStaticInfo.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetStaticInfo.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetStaticInfo(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETSUBINFO: {
                QotGetSubInfo.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetSubInfo.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetSubInfo.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetSubInfo.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetSubInfo(this, serialNo, rsp);
            }
            break;


            case ProtoID.QOT_GETTICKER: {
                QotGetTicker.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetTicker.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetTicker.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetTicker.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetTicker(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETUSERSECURITY: {
                QotGetUserSecurity.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetUserSecurity.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetUserSecurity.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetUserSecurity.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetUserSecurity(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_GETWARRANT: {
                QotGetWarrant.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetWarrant.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetWarrant.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetWarrant.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetWarrant(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_MODIFYUSERSECURITY: {
                QotModifyUserSecurity.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotModifyUserSecurity.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotModifyUserSecurity.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotModifyUserSecurity.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_ModifyUserSecurity(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_REGQOTPUSH: {
                QotRegQotPush.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotRegQotPush.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotRegQotPush.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotRegQotPush.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_RegQotPush(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_REQUESTHISTORYKL: {
                QotRequestHistoryKL.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotRequestHistoryKL.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotRequestHistoryKL.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotRequestHistoryKL.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_RequestHistoryKL(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_REQUESTHISTORYKLQUOTA: {
                QotRequestHistoryKLQuota.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotRequestHistoryKLQuota.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotRequestHistoryKLQuota.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotRequestHistoryKLQuota.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_RequestHistoryKLQuota(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_REQUESTREHAB: {
                QotRequestRehab.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotRequestRehab.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotRequestRehab.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotRequestRehab.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_RequestRehab(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_STOCKFILTER: {
                QotStockFilter.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotStockFilter.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotStockFilter.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotStockFilter.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_StockFilter(this, serialNo, rsp);
            }
            break;

            case ProtoID.QOT_SUB: {
                QotSub.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotSub.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotSub.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotSub.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_Sub(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_GETIPOLIST: {
                QotGetIpoList.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetIpoList.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetIpoList.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetIpoList.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetIpoList(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_GETFUTUREINFO: {
                QotGetFutureInfo.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetFutureInfo.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetFutureInfo.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetFutureInfo.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetFutureInfo(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_REQUESTTRADEDATE: {
                QotRequestTradeDate.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotRequestTradeDate.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotRequestTradeDate.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotRequestTradeDate.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_RequestTradeDate(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_SETPRICEREMINDER: {
                QotSetPriceReminder.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotSetPriceReminder.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotSetPriceReminder.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotSetPriceReminder.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_SetPriceReminder(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_GETPRICEREMINDER: {
                QotGetPriceReminder.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetPriceReminder.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetPriceReminder.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetPriceReminder.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetPriceReminder(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_GETUSERSECURITYGROUP: {
                QotGetUserSecurityGroup.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetUserSecurityGroup.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetUserSecurityGroup.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetUserSecurityGroup.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetUserSecurityGroup(this, serialNo, rsp);
            }
            break;
            case ProtoID.QOT_GETMARKETSTATE: {
                QotGetMarketState.Response rsp;
                if (replyType == ReqReplyType.SvrReply) {
                    try {
                        rsp = QotGetMarketState.Response.parseFrom(data);
                    } catch (InvalidProtocolBufferException e) {
                        rsp = QotGetMarketState.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    }
                } else {
                    rsp = QotGetMarketState.Response.newBuilder().setRetType(replyType.getCode()).build();
                }

                qotSpi.onReply_GetMarketState(this, serialNo, rsp);
            }
            break;
        }
    }

    @Override
    protected synchronized void onPush(ProtoHeader protoHeader, byte[] data) {
        int protoID = protoHeader.nProtoID;

        if (qotSpi == null) {
            return;
        }

        switch (protoID) {
            case ProtoID.NOTIFY: {
                try {
                    Notify.Response rsp = Notify.Response.parseFrom(data);
                    qotSpi.onPush_Notify(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    Notify.Response rsp = Notify.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_Notify(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATEBASICQOT: {
                try {
                    QotUpdateBasicQot.Response rsp = QotUpdateBasicQot.Response.parseFrom(data);
                    qotSpi.onPush_UpdateBasicQuote(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateBasicQot.Response rsp = QotUpdateBasicQot.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateBasicQuote(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATEBROKER: {
                try {
                    QotUpdateBroker.Response rsp = QotUpdateBroker.Response.parseFrom(data);
                    qotSpi.onPush_UpdateBroker(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateBroker.Response rsp = QotUpdateBroker.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateBroker(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATEKL: {
                try {
                    QotUpdateKL.Response rsp = QotUpdateKL.Response.parseFrom(data);
                    qotSpi.onPush_UpdateKL(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateKL.Response rsp = QotUpdateKL.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateKL(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATEORDERBOOK: {
                try {
                    QotUpdateOrderBook.Response rsp = QotUpdateOrderBook.Response.parseFrom(data);
                    qotSpi.onPush_UpdateOrderBook(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateOrderBook.Response rsp = QotUpdateOrderBook.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateOrderBook(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATERT: {
                try {
                    QotUpdateRT.Response rsp = QotUpdateRT.Response.parseFrom(data);
                    qotSpi.onPush_UpdateRT(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateRT.Response rsp = QotUpdateRT.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateRT(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATETICKER: {
                try {
                    QotUpdateTicker.Response rsp = QotUpdateTicker.Response.parseFrom(data);
                    qotSpi.onPush_UpdateTicker(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdateTicker.Response rsp = QotUpdateTicker.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdateTicker(this, rsp);
                }
            }
            break;
            case ProtoID.QOT_UPDATEPRICEREMINDER: {
                try {
                    QotUpdatePriceReminder.Response rsp = QotUpdatePriceReminder.Response.parseFrom(data);
                    qotSpi.onPush_UpdatePriceReminder(this, rsp);
                } catch (InvalidProtocolBufferException e) {
                    QotUpdatePriceReminder.Response rsp = QotUpdatePriceReminder.Response.newBuilder().setRetType(Common.RetType.RetType_Invalid_VALUE).build();
                    qotSpi.onPush_UpdatePriceReminder(this, rsp);
                }
            }
            break;
        }
    }
}

