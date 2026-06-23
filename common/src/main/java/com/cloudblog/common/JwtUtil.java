package com.cloudblog.common;

import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param secret   密钥
     * @param expirationMs 过期时间（毫秒）
     * @return JWT token 字符串
     */
    public static String generateToken(Long userId, String secret, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(DateUtil.offsetMillisecond(now, (int) expirationMs))
                .signWith(getSigningKey(secret))
                .compact();
    }

    /**
     * 解析 Token，获取 Claims
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户ID
     */
    public static Long getUserIdFromToken(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return claims.get("userId", Long.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
