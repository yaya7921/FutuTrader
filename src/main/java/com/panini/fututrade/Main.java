package com.panini.fututrade;

import com.futu.openapi.FTAPI;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.base.FutuBase;
import com.panini.fututrade.strategy.Strategy;
import com.panini.fututrade.strategy.MACDStrategy;

/**
 * @author shuyun
 */
public class Main{

    public static void main(String[] args) {

        FTAPI.init();

        Trader myTrader = new Trader(TrdCommon.TrdEnv.TrdEnv_Simulate,
                                     TrdCommon.TrdMarket.TrdMarket_US,
                                     new MACDStrategy(),
                           "AAPL");

        myTrader.run();

        FTAPI.unInit();
    }

}
