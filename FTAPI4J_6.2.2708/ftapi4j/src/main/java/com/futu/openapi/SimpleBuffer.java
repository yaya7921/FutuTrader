package com.futu.openapi;

class SimpleBuffer {
    byte[] buf;
    int start;
    int length;
    int limit;

    SimpleBuffer(int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit should be greater than 0");

        buf = new byte[limit];
        this.limit = limit;
    }

    void compact() {
        if (length > 0) {
            System.arraycopy(buf, start, buf, 0, length);
        }
        start = 0;
    }

    int append(byte[] src, int srcPos, int srcLen) {
        if (start + length + srcLen > limit)
            compact();
        int available = limit - start - length;
        int copyLen = Math.min(available, srcLen);
        if (copyLen == 0)
            return 0;
        System.arraycopy(src, srcPos, buf, start+length, copyLen);
        length += copyLen;
        return copyLen;
    }

    void consume(int consumption) {
        if (consumption < 0 || consumption > length)
            throw new IllegalArgumentException("Invalid consumption");
        start += consumption;
        length -= consumption;
    }

    void resize(int newLimit) {
        if (newLimit != limit) {
            int len = Math.min(newLimit, length);
            byte[] newBuf = new byte[newLimit];
            System.arraycopy(buf, start, newBuf, 0, len);
            buf = newBuf;
            limit = newLimit;
            length = len;
            start = 0;
        }
    }
}
