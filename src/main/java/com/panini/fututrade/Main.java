package com.panini.fututrade;

import com.futu.openapi.FTAPI;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.strategy.MACDStrategy;

/**
 * @author shuyun
 */
public class Main{

    public static void main(String[] args) {

        FTAPI.init();

        Trader myTrader = new Trader(TrdCommon.TrdEnv.TrdEnv_Simulate,
                                     TrdCommon.TrdMarket.TrdMarket_HK,
                                     TrdCommon.SimAccType.SimAccType_Stock,
                                     new MACDStrategy(),
                           "00700");

        try {
            while(true) {
                Thread task = new Thread() {
                    @Override
                    public void run() {
                        try {
                            myTrader.run();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                task.run();
                task.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        FTAPI.unInit();
    }

}
