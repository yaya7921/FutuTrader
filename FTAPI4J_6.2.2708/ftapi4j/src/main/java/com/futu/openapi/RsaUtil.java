package com.futu.openapi;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.util.ArrayList;

/**
 * RSA pkcs1辅助类
 */
public class RsaUtil {
    /**
     * @param keyFile 加载私钥文件
     * @return KeyPair
     * @throws IOException 读文件失败，或文件内容不是合法的格式
     */
    public static KeyPair loadKeyPair(String keyFile) throws IOException {
        PEMParser pemParser = new PEMParser(new FileReader(keyFile));
        Object keyObj = pemParser.readObject();
        if (keyObj instanceof PEMKeyPair) {
            PEMKeyPair pemKeyPair = (PEMKeyPair)keyObj;
            JcaPEMKeyConverter conv = new JcaPEMKeyConverter().setProvider("BC");
            return conv.getKeyPair(pemKeyPair);
        }
        return null;
    }

    /**
     * @param privateKey 从byte数组中读取私钥
     * @return KeyPair
     * @throws IOException 读文件失败，或文件内容不是合法的格式
     */
    public static KeyPair loadKeyPairFromArray(byte[] privateKey) throws IOException {
        PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(privateKey)));
        Object keyObj = pemParser.readObject();
        if (keyObj instanceof PEMKeyPair) {
            PEMKeyPair pemKeyPair = (PEMKeyPair)keyObj;
            JcaPEMKeyConverter conv = new JcaPEMKeyConverter().setProvider("BC");
            return conv.getKeyPair(pemKeyPair);
        }
        return null;
    }

    /**
     * 解密数据
     * @param src 待解密的数据
     * @param privKey 私钥
     * @return 解密后的数据
     * @throws GeneralSecurityException 解密失败，例如src格式非法
     */
    public static byte[] decrypt(byte[] src, PrivateKey privKey) throws GeneralSecurityException {
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        rsa.init(Cipher.DECRYPT_MODE, privKey);
        final int block = 128;
        ArrayList<Byte> buffer = new ArrayList<>();
        for (int i = 0; i < src.length; i += block) {
            byte[] dec = rsa.doFinal(src, i, block);
            for (byte b : dec) {
                buffer.add(b);
            }
        }

        byte[] result = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); ++i) {
            result[i] = buffer.get(i);
        }
        return result;
    }

    /**
     * 使用公钥加密数据
     * @param src 待加密数据
     * @param privKey 私钥
     * @return 加密后的数据
     * @throws GeneralSecurityException 加密失败
     */
    public static byte[] encrypt(byte[] src, PublicKey privKey) throws GeneralSecurityException {
        Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        rsa.init(Cipher.ENCRYPT_MODE, privKey);
        final int block = 100;
        ArrayList<Byte> buffer = new ArrayList<>();
        for (int i = 0; i < src.length; i += block) {
            int size = block;
            if (src.length - i < 100) {
                size = src.length - i;
            }
            byte[] dec = rsa.doFinal(src, i, size);
            for (byte b : dec) {
                buffer.add(b);
            }
        }

        byte[] result = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); ++i) {
            result[i] = buffer.get(i);
        }
        return result;
    }
}
