package com.futu.openapi;

public enum ReqReplyType {
    SvrReply(0),
    Timeout(-100),
    DisConnect(-200),
    Unknown(-400),
    Invalid(-500);

    final private int code;

    ReqReplyType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ReqReplyType fromCode(int code) {
        for (ReqReplyType item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
