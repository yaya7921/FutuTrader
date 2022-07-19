package com.panini.fututrade;

import com.futu.openapi.FTAPI;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.base.StrategyBase;
import com.panini.fututrade.profile.ProfileConfig;
import com.panini.fututrade.strategy.MACDStrategy;

public class Program {

    public static void main(String[] args) {

        TrdCommon.TrdEnv trdEnv = TrdCommon.TrdEnv.TrdEnv_Simulate;;
        TrdCommon.TrdMarket trdMarket = TrdCommon.TrdMarket.TrdMarket_US;
        MACDStrategy strategy = new MACDStrategy();
        String code = "AAPL";

        FTAPI.init();

        QotCommon.Security security = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_US_Security_VALUE)
                .setCode(code)
                .build();
        run(trdEnv, trdMarket, security, strategy);

        FTAPI.unInit();
    }

    private static void run(TrdCommon.TrdEnv trdEnv,
                            TrdCommon.TrdMarket trdMarket,
                            QotCommon.Security sec,
                            StrategyBase strategy) {
        switch (trdEnv) {
            case TrdEnv_Simulate:
                strategy.runSim(trdMarket, sec);
                break;
            case TrdEnv_Real:
                strategy.runTrade(trdMarket, sec);
                break;
            default:
        }

    }
}
