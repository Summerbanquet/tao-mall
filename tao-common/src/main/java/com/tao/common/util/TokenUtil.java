package com.tao.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 认证工具-基于JWT用户认证TOKEN工具
 *
 * <p>签名密钥不再硬编码：由使用方（tao-web 的 JwtConfig）在启动时
 * 从配置文件读取后调用 {@link #setSecret(String)} 注入。
 * 未注入时使用下面的默认值，保证工具类单独使用时仍可用。</p>
 *
 * @author jacky
 */
public class TokenUtil {

    /**
     * 默认签名密钥（仅在配置未注入时兜底使用，至少 32 字节）
     */
    private static final String DEFAULT_SECRET = "tao-svr-jwt-secret-key-2026-must-be-long-enough-32bytes";

    /** 实际使用的签名密钥 */
    private static String secret = DEFAULT_SECRET;

    /**
     * Token 默认有效期：7 天（毫秒）
     */
    private static final long DEFAULT_EXPIRE_MILLIS = 7L * 24 * 60 * 60 * 1000;

    /** 实际有效期（默认 7 天，可由配置注入） */
    private static long expireMillis = DEFAULT_EXPIRE_MILLIS;

    /**
     * 由配置类注入签名密钥（启动时调用一次）
     */
    public static void setSecret(String secret) {
        if (secret != null && secret.length() >= 32) {
            TokenUtil.secret = secret;
        }
    }

    /**
     * 由配置类注入 Token 有效期（毫秒）
     */
    public static void setExpireMillis(long expireMillis) {
        if (expireMillis > 0) {
            TokenUtil.expireMillis = expireMillis;
        }
    }

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户名生成 TOKEN
     */
    public static String createToken(String userName) {
        return createToken(userName, expireMillis);
    }

    /**
     * 根据用户名生成 TOKEN（指定过期时间，毫秒）
     */
    public static String createToken(String userName, long expireMillis) {
        if (userName == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userName)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMillis))
                .signWith(getKey())
                .compact();
    }

    /**
     * 根据 TOKEN 解析用户名
     * 解析失败（过期、签名错误等）返回 null
     */
    public static String parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 兼容前端可能在 token 前加 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
