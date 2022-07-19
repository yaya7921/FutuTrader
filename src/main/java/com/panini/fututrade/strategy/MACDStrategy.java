package com.panini.fututrade.strategy;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;

/**
 * @author shuyun
 */
public class MACDStrategy extends Strategy {

    public MACDStrategy() {
        this.name = "MACDStrategy";
    }

    @Override
    public void runSim(TrdCommon.TrdMarket trdMarket, QotCommon.Security security) {

    }

    @Override
    public void runTrade(TrdCommon.TrdMarket trdMarket, QotCommon.Security security) {

    }
}
