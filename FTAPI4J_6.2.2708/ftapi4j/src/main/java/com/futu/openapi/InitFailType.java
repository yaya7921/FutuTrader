package com.futu.openapi;

public enum InitFailType {
    UNKNOW(0),             //未知
    TIMEOUT(1),            //超时
    DISCONNECT(3),         //连接断开
    SERIANONOTMATCH(4),    //序列号不符
    SENDINITREQFAILED(4),  //发送初始化协议失败
    OPENDREJECT(5);        //FutuOpenD回包指定错误，具体错误看描述

    final private int code;

    InitFailType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static InitFailType fromCode(int code) {
        for (InitFailType item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}
