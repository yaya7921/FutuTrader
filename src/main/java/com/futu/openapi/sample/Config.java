package com.futu.openapi.sample;

import com.futu.openapi.pb.TrdCommon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MD5Util {
    static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String bytes2Hex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(hexChars[(b >>> 4) & 0xF]);
            sb.append(hexChars[b & 0xF]);
        }
        return sb.toString();
    }

    static String calcMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            return bytes2Hex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}

class Config {
    static long userID = 12345; //牛牛号
    static long trdAcc = 123456L; //业务账号，每个市场都有独立的业务账号，可以通过getAccList获取到。
    static String unlockTradePwdMd5 = MD5Util.calcMD5("123456");  //解锁交易密码的md5
    static TrdCommon.SecurityFirm securityFirm = TrdCommon.SecurityFirm.SecurityFirm_FutuSecurities; //trdAcc所属券商
    static String opendIP = "127.0.0.1";
    static short opendPort = 11111;
    static String rsaKeyFilePath = "";  //RSA私钥文件路径，用于加密和OpenD的连接。
}