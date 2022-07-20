package com.panini.fututrade;

import com.futu.openapi.common.Config;
import com.futu.openapi.pb.*;
import com.panini.fututrade.base.FutuBase;
import com.panini.fututrade.profile.ProfileConfig;
import com.panini.fututrade.strategy.Strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Trader extends FutuBase {

    TrdCommon.TrdEnv trdEnv;
    TrdCommon.TrdMarket trdMarket;
    TrdCommon.SimAccType simAccType;
    Strategy strategy;
    String stockCode;


    Trader(TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, Strategy strategy, String stockCode) {
        this.trdEnv = trdEnv;
        this.trdMarket = trdMarket;
        this.strategy = strategy;
        this.stockCode = stockCode;
    }

    Trader(TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, TrdCommon.SimAccType simAccType, Strategy strategy, String stockCode) {
        this.trdEnv = trdEnv;
        this.trdMarket = trdMarket;
        this.simAccType = simAccType;
        this.strategy = strategy;
        this.stockCode = stockCode;
    }



    public void run() throws InterruptedException {

        int market = QotCommon.QotMarket.QotMarket_Unknown_VALUE;
        switch(this.trdMarket) {
            case TrdMarket_HK:
                market = QotCommon.QotMarket.QotMarket_HK_Security_VALUE;
                break;
            case TrdMarket_US:
                market = QotCommon.QotMarket.QotMarket_US_Security_VALUE;
                break;
            case TrdMarket_CN:
                //默认上证
                market = QotCommon.QotMarket.QotMarket_CNSH_Security_VALUE;
                break;
            default:
        }

        QotCommon.Security security = QotCommon.Security.newBuilder()
                .setMarket(market)
                .setCode(this.stockCode)
                .build();

        System.out.println(security);

        if (security.getMarket() != QotCommon.QotMarket.QotMarket_HK_Security_VALUE &&
                security.getMarket() != QotCommon.QotMarket.QotMarket_US_Security_VALUE) {
            System.err.println("unsupported stock market");
            return;
        }
        boolean ret = initConnectTrdSync(Config.opendIP, Config.opendPort);
        if (!ret) {
            System.err.println("fail to connect trd");
            return;
        }
        ret = initConnectQotSync(Config.opendIP, Config.opendPort);
        if (!ret) {
            System.err.println("fail to connect qot");
            return;
        }

        System.out.printf("qot connect status: %s\n", qotConnStatus);
        System.out.printf("trd connect status: %s\n", trdConnStatus);


        TrdGetAccList.Response arrAccList = getAccListSync();
        System.out.println("waiting for getAccListSync response");
        long accID = findAccNumber(arrAccList.getS2C(), trdEnv, trdMarket, TrdCommon.SimAccType.SimAccType_Stock);
        System.out.printf("accId: %d", accID);
        List<QotCommon.KLine> klines = getKlines(security);

        switch (this.trdEnv) {
            case TrdEnv_Simulate:
                this.strategy.runSim(this, accID, this.trdMarket, security, klines);
                break;
            case TrdEnv_Real:
                TrdUnlockTrade.Response unlockTrdRsp = unlockTradeSync(ProfileConfig.unlockTradePwdMd5, true);
                if (unlockTrdRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("fail to unlock trade; retType=%s msg=%s\n", unlockTrdRsp.getRetType(), unlockTrdRsp.getRetMsg());
                    return;
                }
                this.strategy.runReal(this, accID, this.trdMarket, security, klines);
                break;
            default:
        }

    }

    private List<QotCommon.KLine> getKlines(QotCommon.Security security) throws InterruptedException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(2);
        LocalDateTime endDate = now;
        System.out.println(startDate.format(dateFormatter));
        QotRequestHistoryKL.Response historyKLRsp = requestHistoryKLSync(security, QotCommon.KLType.KLType_1Min,
                QotCommon.RehabType.RehabType_Forward,
                startDate.format(dateFormatter),
                endDate.format(dateFormatter),
                1000,
                null,
                new byte[]{},
                false);
        List<QotCommon.KLine> klines =  historyKLRsp.getS2C().getKlListList();
        //System.out.println(klines);
        return klines;
    }

}
