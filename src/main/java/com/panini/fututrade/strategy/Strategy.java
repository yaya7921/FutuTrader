package com.panini.fututrade.strategy;

import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.panini.fututrade.base.FutuBase;

import java.util.List;

/**
 * @author shuyun
 */
public abstract class Strategy{

    public String name;

    public void runSim(FutuBase client, long accId, TrdCommon.TrdMarket trdMarket, QotCommon.Security security, List<QotCommon.KLine> kLines) throws InterruptedException {
        run(client, accId, TrdCommon.TrdEnv.TrdEnv_Simulate, trdMarket, security, kLines);
    }

    public void runReal(FutuBase client, long accId, TrdCommon.TrdMarket trdMarket, QotCommon.Security security, List<QotCommon.KLine> kLines) throws InterruptedException {
        run(client, accId, TrdCommon.TrdEnv.TrdEnv_Real, trdMarket, security, kLines);
    }

    public abstract void run(FutuBase client, long accId, TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, QotCommon.Security security, List<QotCommon.KLine> kLines) throws InterruptedException;
}
