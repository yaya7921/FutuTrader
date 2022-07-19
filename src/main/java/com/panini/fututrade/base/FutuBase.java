package com.panini.fututrade.base;

import com.futu.openapi.*;
import com.futu.openapi.common.Config;
import com.futu.openapi.common.Connection;
import com.futu.openapi.common.ReqInfo;
import com.futu.openapi.pb.TrdGetAccList;

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

    public TrdGetAccList.Response getAccountList() {
        ReqInfo reqInfo;
        TrdGetAccList.C2S c2s = TrdGetAccList.C2S.newBuilder().setUserID(Config.userID).build();
        TrdGetAccList.Request req = TrdGetAccList.Request.newBuilder().setC2S(c2s).build();
        int sn = trd.getAccList(req);
        if (sn == 0)
            return null;
        reqInfo = new ReqInfo(ProtoID.TRD_GETACCLIST, new Object());
        return (TrdGetAccList.Response) reqInfo.rsp;
    }
}
