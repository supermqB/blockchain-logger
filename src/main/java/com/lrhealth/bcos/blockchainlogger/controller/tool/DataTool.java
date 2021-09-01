package com.lrhealth.bcos.blockchainlogger.controller.tool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.WeakHashMap;

import org.apache.commons.lang3.RandomStringUtils;

public class DataTool {
    private static WeakHashMap<Long, Integer> seqMap = new WeakHashMap<Long, Integer>();

    public static synchronized int getSeq(long ts) {
        Integer seq = seqMap.get(ts);
        if (null == seq) {
            seq = 0;
        }
        seq++;
        seqMap.put(ts, seq);
        return seq;
    }

    private static String hashIt(String content, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return bytesToHex(md.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String md5(String content) {
        return hashIt(content, "MD5");
    }

    public static String sha256(String content) {
        return hashIt(content, "SHA-256");
    }

    public static String signLogConent(String content, String sign) {
        return sha256(content + "." + sign);
    }

    public static String getSignature() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
