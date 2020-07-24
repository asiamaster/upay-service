package com.diligrp.xtrade.upay.boss.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 数据签名验签工具类
 */
public final class SignatureUtils {

    private final static String CHARSET_UTF8 = "utf-8";

    private static final String KEY_ALGORITHM = "RSA";

    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static String sign(String payload, String privateKey) throws Exception {
        byte[] packet = payload.getBytes(CHARSET_UTF8);
        PrivateKey secretKey = getPrivateKey(privateKey);
        return Base64.getEncoder().encodeToString(sign(packet, secretKey));
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey, new SecureRandom());
        signature.update(data);
        return signature.sign();
    }

    public static boolean verify(String payload, String signature, String publicKey) throws Exception {
        byte[] packet = payload.getBytes(CHARSET_UTF8);
        PublicKey secretKey = getPublicKey(publicKey);
        byte[] sign = Base64.getDecoder().decode(signature);
        return verify(packet, sign, secretKey);
    }

    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
    }

    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }
}
