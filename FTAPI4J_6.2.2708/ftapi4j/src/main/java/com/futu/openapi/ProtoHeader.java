package com.futu.openapi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class ProtoHeader {
    public byte[] szHeaderFlag = {'F', 'T'};
    public int nProtoID;
    public byte nProtoFmtType;
    public byte nProtoVer;
    public int nSerialNo;
    public int nBodyLen;
    public byte[] arrBodySHA1 = new byte[20];
    public byte[] arrReserved = {0, 0, 0, 0, 0, 0, 0, 0};

    public static final int HEADER_SIZE = 2 + 4 + 1 + 1 + 4 + 4 + 20 + 8;

    public static ProtoHeader parse(byte[] data, int offset) {
        if (offset + HEADER_SIZE > data.length)
            return null;
        ByteBuffer buffer = ByteBuffer.wrap(data, offset, data.length - offset);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ProtoHeader header = new ProtoHeader();
        buffer.get(header.szHeaderFlag);
        header.nProtoID = buffer.getInt();
        header.nProtoFmtType = buffer.get();
        header.nProtoVer = buffer.get();
        header.nSerialNo = buffer.getInt();
        header.nBodyLen = buffer.getInt();
        buffer.get(header.arrBodySHA1);
        buffer.get(header.arrReserved);
        return header;
    }

    public void write(byte[] dst) {
        if (dst.length < HEADER_SIZE)
            throw new IllegalArgumentException("dst capacity not enough");

        ByteBuffer buffer = ByteBuffer.wrap(dst);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(szHeaderFlag);
        buffer.putInt(nProtoID);
        buffer.put(nProtoFmtType);
        buffer.put(nProtoVer);
        buffer.putInt(nSerialNo);
        buffer.putInt(nBodyLen);
        buffer.put(arrBodySHA1);
        buffer.put(arrReserved);
    }
}