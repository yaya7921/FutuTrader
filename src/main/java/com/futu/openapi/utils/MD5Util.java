package com.futu.openapi.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author shuyun
 */
public class MD5Util {
    public static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String bytes2Hex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(hexChars[(b >>> 4) & 0xF]);
            sb.append(hexChars[b & 0xF]);
        }
        return sb.toString();
    }

    public static String calcMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            return bytes2Hex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
