package com.futu.openapi.sample;

import com.futu.openapi.pb.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MACDUtil {
    static void calcEMA(List<Double> input, int n, List<Double> output) {
        int inputSize = input.size();
        if (inputSize > 0) {
            double lastEMA = input.get(0);
            output.add(lastEMA);
            for (int i = 1; i < inputSize; i++) {
                double curEMA = (input.get(i) * 2 + lastEMA * (n - 1)) / (n + 1);
                output.add(curEMA);
                lastEMA = curEMA;
            }
        }
    }

    static void calcMACD(List<Double> closeList, int shortPeriod, int longPeriod, int smoothPeriod,
                         List<Double> difList, List<Double> deaList, List<Double> macdList) {
        difList.clear();
        deaList.clear();
        macdList.clear();
        List<Double> shortEMA = new ArrayList<>();
        List<Double> longEMA = new ArrayList<>();
        calcEMA(closeList, shortPeriod, shortEMA);
        calcEMA(closeList, longPeriod, longEMA);
        int shortCount = shortEMA.size();
        int longCount = longEMA.size();
        for (int i = 0; i < shortCount && i < longCount; i++) {
            difList.add(shortEMA.get(i) - longEMA.get(i));
        }

        calcEMA(difList, smoothPeriod, deaList);
        int difCount = difList.size();
        int deaCount = deaList.size();
        for (int i = 0; i < difCount && i < deaCount; i++) {
            macdList.add((difList.get(i) - deaList.get(i)) * 2);
        }
    }
}

public class MACDStrategyDemo extends DemoBase {
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
    void run(String unlockTrdPwdMD5, TrdCommon.TrdMarket trdMarket,
             TrdCommon.TrdEnv trdEnv,
             QotCommon.Security sec) {
        System.out.println("Run MACDStrategy");
        try {
            if (sec.getMarket() != QotCommon.QotMarket.QotMarket_HK_Security_VALUE &&
                sec.getMarket() != QotCommon.QotMarket.QotMarket_US_Security_VALUE) {
                System.err.println("unsupported stock market");
                return;
            }
            boolean ret = initConnectTrdSync(Config.opendIP, Config.opendPort);
            if (!ret) {
                System.err.println("fail to connect trd");
                return;
            }
            ret = initConnectQotSync(Config.opendIP, Config.opendPort);
            if (!ret) {
                System.err.println("fail to connect qot");
                return;
            }

            TrdGetAccList.Response arrAccList = getAccListSync();
            long accID = findAccNumber(arrAccList.getS2C(), trdEnv, trdMarket, TrdCommon.SimAccType.SimAccType_Stock);
            if (trdEnv != TrdCommon.TrdEnv.TrdEnv_Simulate) {  // 模拟交易不需要解锁
                TrdUnlockTrade.Response unlockTrdRsp = unlockTradeSync(unlockTrdPwdMD5, true);
                if (unlockTrdRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("fail to unlock trade; retType=%s msg=%s\n", unlockTrdRsp.getRetType(), unlockTrdRsp.getRetMsg());
                    return;
                }
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusDays(100);
            QotRequestHistoryKL.Response historyKLRsp = requestHistoryKLSync(sec, QotCommon.KLType.KLType_Day,
                    QotCommon.RehabType.RehabType_Forward,
                    startDate.format(dateFormatter),
                    now.format(dateFormatter),
                    1000,
                    null,
                    new byte[]{},
                    false);
            ArrayList<Double> klCloseList = new ArrayList<>();
            ArrayList<Double> difList = new ArrayList<>();
            ArrayList<Double> deaList = new ArrayList<>();
            ArrayList<Double> macdList = new ArrayList<>();
            for (QotCommon.KLine kl : historyKLRsp.getS2C().getKlListList()) {
                klCloseList.add(kl.getClosePrice());
            }
            MACDUtil.calcMACD(klCloseList, 12, 26, 9, difList, deaList, macdList);
            int difCount = difList.size();
            int deaCount = deaList.size();
            if (difCount > 0 && deaCount > 0) {
                if (difList.get(difCount - 1) < deaList.get(deaCount - 1) &&
                    difList.get(difCount - 2) > deaList.get(deaCount - 2)) {
                    System.out.println("MACD death cross");
                    TrdCommon.TrdFilterConditions filterConditions = TrdCommon.TrdFilterConditions.newBuilder()
                            .addCodeList(sec.getCode())
                            .build();
                    TrdGetPositionList.Response getPositionListRsp = getPositionListSync(accID, trdMarket,
                            trdEnv, filterConditions, null, null, false);
                    if (getPositionListRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                        System.err.printf("getPositionListSync err; retType=%s msg=%s\n", getPositionListRsp.getRetType(),
                                getPositionListRsp.getRetMsg());
                        return;
                    }
                    for (TrdCommon.Position pstn : getPositionListRsp.getS2C().getPositionListList()) {
                        if (pstn.getCanSellQty() > 0) {
                            System.out.println("Sell holding positions");
                            sell(sec, pstn, accID, trdMarket, trdEnv);
                        }
                    }
                }
                else if (difList.get(difCount - 1) > deaList.get(deaCount - 1) &&
                        difList.get(difCount - 2) < deaList.get(deaCount - 2)) {
                    System.out.println("MACD golden cross");
                    buy(sec, accID, trdMarket, trdEnv);
                }
                else {
                    System.out.println("MACD does not cross");
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
        System.out.println("MACDStrategy End");
    }

    void sell(QotCommon.Security sec, TrdCommon.Position pstn, long trdAcc, TrdCommon.TrdMarket trdMarket,
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

    void buy(QotCommon.Security sec, long trdAcc, TrdCommon.TrdMarket trdMarket,
             TrdCommon.TrdEnv trdEnv) throws  InterruptedException {
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
}
