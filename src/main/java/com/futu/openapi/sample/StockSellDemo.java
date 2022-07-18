package com.futu.openapi.sample;

import com.futu.openapi.common.Config;
import com.futu.openapi.pb.*;

import java.util.ArrayList;
import java.util.Arrays;

public class StockSellDemo extends DemoBase {
    long findAccNumber(TrdGetAccList.S2C s2c, TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket,
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

    void simpleSell(String code, QotCommon.QotMarket qotMarket, TrdCommon.TrdSecMarket secMarket,
                    double price, int volume, TrdCommon.OrderType orderType,
                    TrdCommon.TrdEnv trdEnv,
                    TrdCommon.TrdMarket trdMarket,
                    String trdPwdMD5) {
        System.out.println("Run simpleSell");
        try {
            boolean ret = initConnectQotSync(Config.opendIP, (short)Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            }
            ret = initConnectTrdSync(Config.opendIP, (short)Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            }

            TrdGetAccList.Response arrAccList = getAccListSync();
            long accID = findAccNumber(arrAccList.getS2C(), trdEnv, trdMarket, TrdCommon.SimAccType.SimAccType_Stock);
            int lotSize = 0;
            QotCommon.Security sec = makeSec(qotMarket, code);
            while (true) {
                Thread.sleep(1000);
                if (lotSize == 0) {
                    ArrayList<QotCommon.Security> secList = new ArrayList<>();
                    secList.add(sec);
                    QotGetSecuritySnapshot.Response rsp = getSecuritySnapshotSync(secList);
                    if (rsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                        System.err.printf("getSecuritySnapshotSync err; retType=%d msg=%s\n", rsp.getRetType(),
                                rsp.getRetMsg());
                        return;
                    }
                    lotSize = rsp.getS2C().getSnapshotList(0).getBasic().getLotSize();
                    if (lotSize <= 0) {
                        System.err.printf("invalid lot size; code=%s lotSize=%d\n", code, lotSize);
                        return;
                    }
                }

                int qty = (volume / lotSize) * lotSize; // 将数量调整为整手的股数
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, accID, trdMarket);
                TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                        .setHeader(trdHeader)
                        .setPacketID(trd.nextPacketID())
                        .setTrdSide(TrdCommon.TrdSide.TrdSide_Sell_VALUE)
                        .setOrderType(orderType.getNumber())
                        .setCode(code)
                        .setQty(qty)
                        .setPrice(price)
                        .setAdjustPrice(true)
                        .setSecMarket(secMarket.getNumber())
                        .build();
                TrdPlaceOrder.Response placeOrderRsp = placeOrderSync(c2s);
                if (placeOrderRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.out.printf("placeOrderSync err; retType=%d msg=%s\n", placeOrderRsp.getRetType(), placeOrderRsp.getRetMsg());
                } else {
                    System.out.printf("placeOrderSync succeed: %s\n", placeOrderRsp.getS2C());
                }
                return;
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        System.out.println("SimpleSell End");
    }

    void smartSell(String code, QotCommon.QotMarket qotMarket, TrdCommon.TrdSecMarket secMarket,
                   int volume, TrdCommon.OrderType orderType,
                   TrdCommon.TrdEnv trdEnv,
                   TrdCommon.TrdMarket trdMarket,
                   String trdPwdMD5) {
        try {
            boolean ret = initConnectQotSync(Config.opendIP, (short)Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            }
            ret = initConnectTrdSync(Config.opendIP, (short)Config.opendPort);
            if (!ret) {
                System.err.println("Fail to connect opend");
                return;
            }
            TrdGetAccList.Response arrAccList = getAccListSync();
            long accID = findAccNumber(arrAccList.getS2C(), trdEnv, trdMarket, TrdCommon.SimAccType.SimAccType_Stock);

            int lotSize = 0;
            QotCommon.Security sec = makeSec(qotMarket, code);
            while (true) {
                Thread.sleep(1000);
                if (lotSize == 0) {
                    ArrayList<QotCommon.Security> secList = new ArrayList<>();
                    secList.add(sec);
                    QotGetSecuritySnapshot.Response rsp = getSecuritySnapshotSync(secList);
                    if (rsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                        System.err.printf("getSecuritySnapshotSync err; retType=%d msg=%s\n", rsp.getRetType(),
                                rsp.getRetMsg());
                        return;
                    }
                    lotSize = rsp.getS2C().getSnapshotList(0).getBasic().getLotSize();
                    if (lotSize <= 0) {
                        System.err.printf("invalid lot size; code=%s lotSize=%d\n", code, lotSize);
                        return;
                    }
                }
                int qty = (volume / lotSize) * lotSize; // 将数量调整为整手的股数

                QotSub.Response subRsp = subSync(Arrays.asList(sec),
                        Arrays.asList(QotCommon.SubType.SubType_OrderBook),
                        true,
                        false);
                if (subRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("subSync er; retType=%s; msg=%s\n", subRsp.getRetType(), subRsp.getRetMsg());
                    return;
                }

                QotGetOrderBook.Response getOrderBookRsp = getOrderBookSync(makeSec(qotMarket, code), 1);
                if (getOrderBookRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("getOrderBookSync er; retType=%s; msg=%s\n", subRsp.getRetType(), subRsp.getRetMsg());
                    return;
                }
                double bid1Price = getOrderBookRsp.getS2C().getOrderBookBidList(0).getPrice();
                TrdCommon.TrdHeader trdHeader = makeTrdHeader(trdEnv, 123456, trdMarket);
                TrdPlaceOrder.C2S c2s = TrdPlaceOrder.C2S.newBuilder()
                        .setHeader(trdHeader)
                        .setPacketID(trd.nextPacketID())
                        .setTrdSide(TrdCommon.TrdSide.TrdSide_Sell_VALUE)
                        .setOrderType(orderType.getNumber())
                        .setCode(code)
                        .setQty(qty)
                        .setPrice(bid1Price)
                        .setAdjustPrice(true)
                        .setSecMarket(secMarket.getNumber())
                        .build();
                TrdPlaceOrder.Response placeOrderRsp = placeOrderSync(c2s);
                if (placeOrderRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("placeOrderSync err; retType=%d msg=%s\n", placeOrderRsp.getRetType(), placeOrderRsp.getRetMsg());
                } else {
                    System.out.printf("下单成功: %s\n", placeOrderRsp.getS2C());
                }
                return;
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}
