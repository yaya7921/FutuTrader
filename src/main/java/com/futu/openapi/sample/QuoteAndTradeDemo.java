package com.futu.openapi.sample;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.common.Config;
import com.futu.openapi.pb.*;

import java.util.Arrays;
import java.util.List;

public class QuoteAndTradeDemo extends DemoBase {
    void quoteTest() {
        System.out.println("Run SubAndQuote");
        try {
            boolean ret = initConnectQotSync(Config.opendIP, Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            }
            List<QotCommon.Security> secArr = Arrays.asList(
                    makeSec(QotCommon.QotMarket.QotMarket_HK_Security, "00388"),
                    makeSec(QotCommon.QotMarket.QotMarket_HK_Security, "00700"),
                    makeSec(QotCommon.QotMarket.QotMarket_HK_Security, "HSImain")
            );
            List<QotCommon.SubType> subTypes = Arrays.asList(
                    QotCommon.SubType.SubType_Basic,
                    QotCommon.SubType.SubType_Broker,
                    QotCommon.SubType.SubType_OrderBook,
                    QotCommon.SubType.SubType_RT,
                    QotCommon.SubType.SubType_KL_Day,
                    QotCommon.SubType.SubType_Ticker
            );
            QotSub.Response subRsp = subSync(secArr, subTypes, true, true);
            if (subRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                System.err.printf("subSync err; retType=%s msg=%s\n", subRsp.getRetType(), subRsp.getRetMsg());
            }
        }
        catch (InterruptedException e) {
            System.err.println("Interrupted");
            return;
        }
    }

    void tradeHkTest() {
        try {
            boolean ret = initConnectTrdSync(Config.opendIP, Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            } else {
                System.out.println("trd connected");
            }

            TrdUnlockTrade.Response unlockTradeRsp = unlockTradeSync(Config.unlockTradePwdMd5, true);
            if (unlockTradeRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                System.err.printf("unlockTradeSync err; retType=%s msg=%s\n", unlockTradeRsp.getRetType(), unlockTradeRsp.getRetMsg());
            } else {
                System.out.println("unlock succeed");
            }

            TrdGetFunds.Response getFundsRsp = getFundsSync(Config.trdAcc, TrdCommon.TrdMarket.TrdMarket_HK, TrdCommon.TrdEnv.TrdEnv_Real,
                    false, TrdCommon.Currency.Currency_Unknown);
            System.out.printf("getFundsSync: %s\n", getFundsRsp);

            TrdGetAccList.Response getAccListRsp = getAccListSync();
            System.out.printf("getAccList: %s\n", getAccListRsp);

            {
                TrdGetPositionList.Response getPositionListRsp = getPositionListSync(Config.trdAcc,
                        TrdCommon.TrdMarket.TrdMarket_HK,
                        TrdCommon.TrdEnv.TrdEnv_Real, null,
                        -50.0, 50.0, false);
                System.out.printf("getPositionList: %s\n", getPositionListRsp);
            }

            {
                TrdGetOrderList.Response getOrderListRsp = getOrderListSync(Config.trdAcc, TrdCommon.TrdMarket.TrdMarket_HK,
                        TrdCommon.TrdEnv.TrdEnv_Real, false, null,
                        Arrays.asList(TrdCommon.OrderStatus.OrderStatus_Submitted));
                System.out.printf("getOrderList: %s\n", getOrderListRsp);
            }

            {
                TrdCommon.TrdHeader header = TrdCommon.TrdHeader.newBuilder()
                        .setTrdEnv(TrdCommon.TrdEnv.TrdEnv_Real_VALUE)
                        .setAccID(Config.trdAcc)
                        .setTrdMarket(TrdCommon.TrdMarket.TrdMarket_HK_VALUE)
                        .build();
                TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                        .setPacketID(trd.nextPacketID())
                        .setHeader(header)
                        .setTrdSide(TrdCommon.TrdSide.TrdSide_Sell_VALUE)
                        .setOrderType(TrdCommon.OrderType.OrderType_Normal_VALUE)
                        .setCode("00700")
                        .setQty(100)
                        .setPrice(700)
                        .setAdjustPrice(true)
                        .setSecMarket(TrdCommon.TrdSecMarket.TrdSecMarket_HK_VALUE)
                        .build();
                TrdPlaceOrder.Response placeOrderRsp = placeOrderSync(c2s);
                System.out.printf("placeOrder: %s\n", placeOrderRsp);
            }

            {
                TrdCommon.TrdFilterConditions filterConditions = TrdCommon.TrdFilterConditions.newBuilder()
                        .addCodeList("00700")
                        .build();
                TrdGetOrderFillList.Response getOrderFillListRsp = getOrderFillListSync(Config.trdAcc,
                        TrdCommon.TrdMarket.TrdMarket_HK,
                        TrdCommon.TrdEnv.TrdEnv_Real, false, filterConditions);
                System.out.printf("getOrderFillList: %s\n", getOrderFillListRsp);
            }
        }
        catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
    }

    @Override
    public void onPush_UpdateOrderBook(FTAPI_Conn client, QotUpdateOrderBook.Response rsp) {
        System.out.printf("onPush_UpdateOrderBook: ask1: %f, bid1: %f\n",
                rsp.getS2C().getOrderBookAskList(0).getPrice(),
                rsp.getS2C().getOrderBookBidList(0).getPrice());
    }

    @Override
    public void onPush_UpdateBasicQuote(FTAPI_Conn client, QotUpdateBasicQot.Response rsp) {
        System.out.printf("onPush_UpdateBasicQuote: code: %s, high: %f, open: %f, low: %f, cur: %f\n",
                rsp.getS2C().getBasicQotList(0).getSecurity().getCode(),
                rsp.getS2C().getBasicQotList(0).getHighPrice(),
                rsp.getS2C().getBasicQotList(0).getOpenPrice(),
                rsp.getS2C().getBasicQotList(0).getLowPrice(),
                rsp.getS2C().getBasicQotList(0).getCurPrice());
    }

    @Override
    public void onPush_UpdateKL(FTAPI_Conn client, QotUpdateKL.Response rsp) {
        System.out.printf("onPush_UpdateKL: code: %s, close: %f, volume: %d\n",
                rsp.getS2C().getSecurity().getCode(),
                rsp.getS2C().getKlList(0).getClosePrice(),
                rsp.getS2C().getKlList(0).getVolume());
    }

    @Override
    public void onPush_UpdateRT(FTAPI_Conn client, QotUpdateRT.Response rsp) {
        System.out.printf("onPush_UpdateRT: time: %s, price: %f\n",
                rsp.getS2C().getRtList(0).getTime(),
                rsp.getS2C().getRtList(0).getPrice());
    }

    @Override
    public void onPush_UpdateTicker(FTAPI_Conn client, QotUpdateTicker.Response rsp) {
        System.out.printf("onPush_UpdateTicker: time: %s, dir: %d, price: %f\n",
                rsp.getS2C().getTickerList(0).getTime(),
                rsp.getS2C().getTickerList(0).getDir(),
                rsp.getS2C().getTickerList(0).getPrice());
    }

    @Override
    public void onPush_UpdateBroker(FTAPI_Conn client, QotUpdateBroker.Response rsp) {
        System.out.printf("onPush_UpdateBroker: ask_broker1: %s, bid_broker1: %s\n",
                rsp.getS2C().getBrokerAskList(0).getName(),
                rsp.getS2C().getBrokerBidList(0).getName());
    }

    @Override
    public void onPush_UpdateOrder(FTAPI_Conn client, TrdUpdateOrder.Response rsp) {
        System.out.printf("onPush_UpdateOrder: %s\n", rsp);
    }

    @Override
    public void onPush_UpdateOrderFill(FTAPI_Conn client, TrdUpdateOrderFill.Response rsp) {
        System.out.printf("onPush_UpdateOrderFill: %s\n", rsp);
    }
}
