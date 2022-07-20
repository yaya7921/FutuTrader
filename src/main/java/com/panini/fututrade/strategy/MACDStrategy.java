package com.panini.fututrade.strategy;

import com.futu.openapi.pb.Common;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.pb.TrdGetPositionList;
import com.futu.openapi.utils.MACDUtil;
import com.panini.fututrade.base.FutuBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuyun
 */
public class MACDStrategy extends Strategy {

    public MACDStrategy() {
        this.name = "MACDStrategy";
    }

    @Override
    public void run(FutuBase client, long accId, TrdCommon.TrdEnv trdEnv, TrdCommon.TrdMarket trdMarket, QotCommon.Security security, List<QotCommon.KLine> kLines) throws InterruptedException {
        ArrayList<Double> klCloseList = new ArrayList<>();
        ArrayList<Double> difList = new ArrayList<>();
        ArrayList<Double> deaList = new ArrayList<>();
        ArrayList<Double> macdList = new ArrayList<>();
        for (QotCommon.KLine kl : kLines) {
            klCloseList.add(kl.getClosePrice());
        }
        MACDUtil.calcMACD(klCloseList, 12, 26, 9, difList, deaList, macdList);
        int difCount = difList.size();
        int deaCount = deaList.size();
        if (difCount > 0 && deaCount > 0) {
            System.out.printf("difList.get(difCount - 1) is %f \n", difList.get(difCount - 1));
            System.out.printf("deaList.get(deaCount - 1) is %f \n", deaList.get(deaCount - 1));
            System.out.printf("difList.get(difCount - 2) is %f \n", difList.get(difCount - 2));
            System.out.printf("deaList.get(deaCount - 2) is %f \n", deaList.get(deaCount - 2));
            if (difList.get(difCount - 1) < deaList.get(deaCount - 1) &&
                    difList.get(difCount - 2) > deaList.get(deaCount - 2)) {
                System.out.println("MACD death cross");
                TrdCommon.TrdFilterConditions filterConditions = TrdCommon.TrdFilterConditions.newBuilder()
                        .addCodeList(security.getCode())
                        .build();
                TrdGetPositionList.Response getPositionListRsp = client.getPositionListSync(accId, trdMarket,
                        TrdCommon.TrdEnv.TrdEnv_Simulate, filterConditions, null, null, false);
                if (getPositionListRsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("getPositionListSync err; retType=%s msg=%s\n", getPositionListRsp.getRetType(),
                            getPositionListRsp.getRetMsg());
                    return;
                }
                for (TrdCommon.Position pstn : getPositionListRsp.getS2C().getPositionListList()) {
                    if (pstn.getCanSellQty() > 0) {
                        System.out.println("Sell holding positions");
                        client.sell(security, pstn, accId, trdMarket, trdEnv);
                    }
                }
            }
            else if (difList.get(difCount - 1) > deaList.get(deaCount - 1) &&
                    difList.get(difCount - 2) < deaList.get(deaCount - 2)) {
                System.out.println("MACD golden cross");
                client.buy(security, accId, trdMarket, trdEnv);
            }
            else {
                System.out.println("MACD does not cross");
            }
        }
    }
}
