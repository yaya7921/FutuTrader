package com.panini.fututrade.strategy;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;

/**
 * @author shuyun
 */
public abstract class Strategy {

    public String name;

    public abstract void runSim(long accId, TrdCommon.TrdMarket trdMarket, QotCommon.Security security);

    public abstract void runTrade(long accId, TrdCommon.TrdMarket trdMarket, QotCommon.Security security);
}
