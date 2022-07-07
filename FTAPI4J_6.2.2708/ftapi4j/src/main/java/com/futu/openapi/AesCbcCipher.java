package com.futu.openapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

/**
 * AES CBC加密，用于协议包体加解密（不包括1001协议）
 */
public class AesCbcCipher {
    private byte[] iv;
    private Key key;

    /**
     * @param key key
     * @param iv iv
     */
    AesCbcCipher(byte[] key, byte[] iv) {
        this.key = new SecretKeySpec(key, "AES");
        this.iv = iv;
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 加密
     * @param input 待加密数据
     * @return 加密后的数据
     * @throws GeneralSecurityException 加密失败
     */
    byte[] encrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.iv));
//        input = pkcs7Padding(input, cipher.getBlockSize());
        return cipher.doFinal(input);
    }

    /**
     * 解密数据
     * @param input 待解密数据
     * @return 解密后的数据
     * @throws GeneralSecurityException 解密失败
     */
    byte[] decrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(this.iv));
        byte[] output = cipher.doFinal(input);
//        return pkcs7Unpadding(output);
        return output;
    }

    byte[] pkcs7Padding(byte[] input, int blockSize) {
        int paddingLen = blockSize - input.length % blockSize;
        byte[] output = new byte[input.length + paddingLen];
        System.arraycopy(input, 0, output, 0, input.length);
        Arrays.fill(output, input.length, output.length, (byte)paddingLen);
        return output;
    }

    byte[] pkcs7Unpadding(byte[] input) {
        int paddingLen = (int)input[input.length-1];
        byte[] output = new byte[input.length - paddingLen];
        System.arraycopy(input, 0, output, 0, output.length);
        return output;
    }
}
