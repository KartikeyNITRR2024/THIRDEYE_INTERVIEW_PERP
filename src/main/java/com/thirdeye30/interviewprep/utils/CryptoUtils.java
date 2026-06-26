package com.thirdeye30.interviewprep.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {

    private static final String ALGORITHM = "AES";

    public static byte[] encryptDocument(byte[] data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.substring(0, 16).getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptDocument(byte[] data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.substring(0, 16).getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
}

