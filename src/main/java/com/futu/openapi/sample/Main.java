package com.futu.openapi.sample;

import com.futu.openapi.FTAPI;
import com.futu.openapi.common.Config;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;

public class Main {
    static String[] parameter = {"SecuritySnapshot", "MACDStrategy", "QuoteAndTrade", "StockSell"};
    public static void main(String[] args) throws Exception {

        FTAPI.init();

        int nDemoIndex = 1;
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
