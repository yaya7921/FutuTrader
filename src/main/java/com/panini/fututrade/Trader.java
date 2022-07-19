package com.panini.fututrade;

import com.futu.openapi.pb.*;
import com.panini.fututrade.base.FutuBase;
import com.panini.fututrade.profile.ProfileConfig;
import com.panini.fututrade.strategy.Strategy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        List<QotCommon.KLine> klines = getKlines();

        switch (this.trdEnv) {
            case TrdEnv_Simulate:
                accId = getAccIdSim(trdMarket, simAccType);
                this.strategy.runSim(accId, this.trdMarket, this.security, klines);
                break;
            case TrdEnv_Real:
                accId = getAccId(trdMarket);
                TrdUnlockTrade.Response unlockTrdRsp = unlockTrade(ProfileConfig.unlockTradePwdMd5, true);
                if (unlockTrdRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("fail to unlock trade; retType=%s msg=%s\n", unlockTrdRsp.getRetType(), unlockTrdRsp.getRetMsg());
                    return;
                }
                this.strategy.runReal(accId, this.trdMarket, this.security, klines);
                break;
            default:
        }

    }

    private List<QotCommon.KLine> getKlines() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(100);
        QotRequestHistoryKL.Response historyKLRsp = requestHistoryKL(security, QotCommon.KLType.KLType_Day,
                QotCommon.RehabType.RehabType_Forward,
                startDate.format(dateFormatter),
                now.format(dateFormatter),
                1000,
                null,
                new byte[]{},
                false);
        return historyKLRsp.getS2C().getKlListList();
    }

}
