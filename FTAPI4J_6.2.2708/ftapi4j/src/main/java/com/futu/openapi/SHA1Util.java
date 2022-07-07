package com.futu.openapi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算sha1辅助类
 */
public class SHA1Util {
    /**
     * 计算input的sha1
     * @param input 待计算数据
     * @return 得到的sha1
     * @throws NoSuchAlgorithmException 使用的jvm不支持sha1
     */
    public static byte[] calc(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(input);
        return sha1.digest();
    }
}
