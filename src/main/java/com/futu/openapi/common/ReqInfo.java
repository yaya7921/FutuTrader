package com.futu.openapi.common;

import com.google.protobuf.GeneratedMessageV3;

public class ReqInfo {
    public int protoID;
    public Object syncEvent;
    public GeneratedMessageV3 rsp;

    public ReqInfo(int protoID, Object syncEvent) {
        this.protoID = protoID;
        this.syncEvent = syncEvent;
    }
}
