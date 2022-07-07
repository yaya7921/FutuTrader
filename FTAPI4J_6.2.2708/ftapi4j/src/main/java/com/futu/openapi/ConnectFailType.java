package com.futu.openapi;

public enum ConnectFailType {
    UNKNOWN(-1),
    NONE(0),
    CREATEFAILED(1),
    CLOSEFAILED(2),
    SHUTDOWNFAILED(3),
    GETHOSTBYNAMEFAILED(4),
    GETHOSTBYNAMEWRONG(5),
    CONNECTFAILED(6),
    BINDFAILED(7),
    LISTENFAILED(8),
    SELECTRETURNERROR(9),
    SENDFAILED(10),
    RECVFAILED(11);

    final private int code;

    ConnectFailType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ConnectFailType fromCode(int code) {
        for (ConnectFailType item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
