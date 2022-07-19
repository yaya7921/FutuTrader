package com.panini.fututrade;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.base.FutuBase;
import com.panini.fututrade.profile.ProfileConfig;
import com.panini.fututrade.strategy.Strategy;

public class Trader extends FutuBase {

    TrdCommon.TrdEnv trdEnv;
    TrdCommon.TrdMarket trdMarket;
    TrdCommon.SimAccType simAccType;
    QotCommon.Security security;
    Strategy strategy;
    String stockCode;


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

    Trader(TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, TrdCommon.SimAccType simAccType, Strategy strategy, String stockCode) {
        this.trdEnv = trdEnv;
        this.trdMarket = trdMarket;
        this.simAccType = simAccType;
        this.strategy = strategy;
        this.stockCode = stockCode;
        this.security = QotCommon.Security.newBuilder()
                .setMarket(QotCommon.QotMarket.QotMarket_US_Security_VALUE)
                .setCode(stockCode)
                .build();
    }



    public void run() {

        if(!initConnectTrd(ProfileConfig.opendIP, ProfileConfig.opendPort)) {
            System.err.println("init trade connect failed");
            return;
        }
        if(!initConnectQot(ProfileConfig.opendIP, ProfileConfig.opendPort)) {
            System.err.println("init qot connect failed");
            return;
        }

        long accId = 0;

        switch (this.trdEnv) {
            case TrdEnv_Simulate:
                accId = getAccIdSim(trdMarket, simAccType);
                this.strategy.runSim(accId, this.trdMarket, this.security);
                break;
            case TrdEnv_Real:
                accId = getAccId(trdMarket);
                this.strategy.runTrade(accId, this.trdMarket, this.security);
                break;
            default:
        }

    }


}
