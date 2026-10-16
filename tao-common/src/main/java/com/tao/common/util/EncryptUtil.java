package com.tao.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具-MD5加盐加密
 */
public class EncryptUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成随机盐
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 对密码进行 MD5 加盐加密
     * 格式：{salt}$${md5(password+salt)}
     */
    public static String encrypt(String data) {
        if (data == null) {
            return null;
        }
        // 默认 8 字节盐
        return encrypt(data, generateSalt(8));
    }

    /**
     * 对密码进行 MD5 加盐加密（指定盐）
     */
    public static String encrypt(String data, String salt) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(salt.getBytes());
            byte[] digest = md5.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return salt + "$$" + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 校验明文与密文是否匹配
     */
    public static boolean matches(String plain, String encrypted) {
        if (plain == null || encrypted == null || !encrypted.contains("$$")) {
            return false;
        }
        String salt = encrypted.split("\\$\\$")[0];
        return encrypted.equals(encrypt(plain, salt));
    }
}