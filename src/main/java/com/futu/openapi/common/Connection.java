package com.futu.openapi.common;

/**
 * @author shuyun
 */
public class Connection {
    public class ConnError extends RuntimeException {
        private final long errCode;

        ConnError(long errCode, String desc) {
            super(desc);
            this.errCode = errCode;
        }

        long getErrCode() {
            return errCode;
        }
    }

    public enum ConnStatus {
        DISCONNECT,
        READY
    }
}

