package com.futu.openapi.sample;

import com.futu.openapi.pb.Common;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetSecuritySnapshot;
import com.futu.openapi.pb.QotGetStaticInfo;

import java.util.ArrayList;
import java.util.List;

public class GetSecuritySnapshotDemo extends DemoBase {
    void run(QotCommon.QotMarket market) {
        System.out.println("Run SecuritySnapshot");
        try {
            boolean ret = initConnectQotSync("127.0.0.1", (short)11111);
            if (ret) {
                System.out.println("qot connected");
            } else {
                System.out.println("fail to connect opend");
                return;
            }

            int[] stockTypes = {QotCommon.SecurityType.SecurityType_Eqty_VALUE,
                    QotCommon.SecurityType.SecurityType_Index_VALUE,
            QotCommon.SecurityType.SecurityType_Trust_VALUE,
                    QotCommon.SecurityType.SecurityType_Warrant_VALUE,
            QotCommon.SecurityType.SecurityType_Bond_VALUE};
            ArrayList<QotCommon.Security> stockCodes = new ArrayList<>();
            for (int stockType : stockTypes) {
                QotGetStaticInfo.C2S c2s = QotGetStaticInfo.C2S.newBuilder()
                        .setMarket(market.getNumber())
                        .setSecType(stockType)
                        .build();
                QotGetStaticInfo.Response rsp = getStaticInfoSync(c2s);
                if (rsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("getStaticInfoSync fail: %s\n", rsp.getRetMsg());
                    return;
                }
                for (QotCommon.SecurityStaticInfo info : rsp.getS2C().getStaticInfoListList()) {
                    stockCodes.add(info.getBasic().getSecurity());
                }
            }

            if (stockCodes.size() == 0) {
                System.err.printf("Error market:'%s' can not get stock info ", market);
                return;
            }

            int nCount = 0;
            for (int i = 0; i < stockCodes.size(); i += 200) {
                int count = i + 200 <= stockCodes.size() ? 200 : stockCodes.size() - i;
                List<QotCommon.Security> codes = stockCodes.subList(i, i+count);
                QotGetSecuritySnapshot.Response rsp = getSecuritySnapshotSync(codes);
                if (rsp.getRetType() != Common.RetType.RetType_Succeed_VALUE) {
                    System.err.printf("getSecuritySnapshotSync err: retType=%d msg=%s\n", rsp.getRetType(), rsp.getRetMsg());
                } else {
                    for (QotGetSecuritySnapshot.Snapshot snapshot : rsp.getS2C().getSnapshotListList()) {
//                        System.out.println(snapshot.toString());
                        System.out.printf("code: %s\n", snapshot.getBasic().getSecurity().getCode());
                        System.out.printf("price: %f\n", snapshot.getBasic().getCurPrice());
                        nCount++;
                        if (nCount >= 100) {
                            return;
                        }
                    }
                }
                Thread.sleep(3000);
            }
        }
        catch (InterruptedException e) {

        }
        System.out.println("SecuritySnapshot End");
    }
}
