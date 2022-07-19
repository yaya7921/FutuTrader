package com.panini.fututrade.base;

import com.futu.openapi.*;
import com.futu.openapi.common.Connection;

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
}
