package com.panini.fututrade.base;

import com.futu.openapi.*;
import com.futu.openapi.common.Config;
import com.futu.openapi.common.Connection;
import com.futu.openapi.common.ReqInfo;
import com.futu.openapi.pb.TrdCommon;
import com.futu.openapi.pb.TrdGetAccList;
import com.futu.openapi.pb.TrdUnlockTrade;

/**
 * @author shuyun
 */
public class FutuBase implements FTSPI_Conn, FTSPI_Qot, FTSPI_Trd {
    protected FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();
    protected FTAPI_Conn_Trd trd = new FTAPI_Conn_Trd();
    protected Connection.ConnStatus qotStatus = Connection.ConnStatus.DISCONNECT;
    protected Connection.ConnStatus trdStatus = Connection.ConnStatus.DISCONNECT;

    public boolean initConnectQot(String ip, short port) {
        qot.setConnSpi(this);
        qot.setQotSpi(this);
        return qot.initConnect(ip, port, false);
    }

    public boolean initConnectTrd(String ip, short port) {
        trd.setConnSpi(this);
        trd.setTrdSpi(this);
        return trd.initConnect(ip, port, false);
    }

    public void unlockTrade() {
        TrdUnlockTrade.C2S c2s = TrdUnlockTrade.C2S.newBuilder()
                .setUnlock(true)
                .setPwdMD5(Config.unlockTradePwdMd5)
                .setSecurityFirm(Config.securityFirm.getNumber())
                .build();
        TrdUnlockTrade.Request req = TrdUnlockTrade.Request.newBuilder().setC2S(c2s).build();
        trd.unlockTrade(req);
    }

    public TrdGetAccList.Response getAccList() {
        ReqInfo reqInfo;
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(Config.userID).build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.getAccList(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_GETACCLIST, new Object());
        return (TrdGetAccList.Response) reqInfo.rsp;
    }

    public long getAccId(TrdCommon.TrdMarket trdMarket) {
        TrdGetAccList.Response arrAccList = getAccList();
        TrdGetAccList.S2C s2c = arrAccList.getS2C();
        int nLength = s2c.getAccListCount();
        for (int i = 0; i < nLength; i++) {
            if (s2c.getAccList(i).getTrdEnv() == TrdCommon.TrdEnv.TrdEnv_Real_VALUE &&
                    s2c.getAccList(i).getTrdMarketAuthList(0) == trdMarket.getNumber()) {
                return s2c.getAccList(i).getAccID();
            }
        }
        return 0;
    }

    public long getAccIdSim(TrdCommon.TrdMarket trdMarket,
                            TrdCommon.SimAccType simAccType) {
        TrdGetAccList.Response arrAccList = getAccList();
        TrdGetAccList.S2C s2c = arrAccList.getS2C();
        int nLength = s2c.getAccListCount();
        for (int i = 0; i < nLength; i++) {
            if (s2c.getAccList(i).getTrdEnv() == TrdCommon.TrdEnv.TrdEnv_Simulate_VALUE &&
                    s2c.getAccList(i).getTrdMarketAuthList(0) == trdMarket.getNumber()) {
                if (s2c.getAccList(i).getSimAccType() == simAccType.getNumber()) {
                    return s2c.getAccList(i).getAccID();
                }
            }
        }
        return 0;
    }
}
