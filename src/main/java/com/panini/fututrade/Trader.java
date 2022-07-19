package com.panini.fututrade;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.base.FutuBase;
import com.panini.fututrade.strategy.MACDStrategy;
import com.panini.fututrade.strategy.Strategy;

public class Trader extends FutuBase {

    TrdCommon.TrdEnv trdEnv;
    TrdCommon.TrdMarket trdMarket;
    Strategy strategy;
    String stockCode = "AAPL";
    QotCommon.Security security;

    Trader(TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, Strategy strategy, String stockCode) {
        this.trdEnv = trdEnv;
        this.trdMarket = trdMarket;
        this.strategy = strategy;
        this.stockCode = stockCode;
        this.security = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_US_Security_VALUE)
                .setCode(stockCode)
                .build();
    }

    public void run() {

        switch (this.trdEnv) {
            case TrdEnv_Simulate:
                this.strategy.runSim(this.trdMarket, this.security);
                break;
            case TrdEnv_Real:
                this.strategy.runTrade(this.trdMarket, this.security);
                break;
            default:
        }

    }


}
