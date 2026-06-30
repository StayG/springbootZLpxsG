package com.web.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、解析和验证 JWT Token
 */
@Component
public class JwtUtil {

    /**
     * 密钥（从配置文件读取，生产环境应该使用环境变量或配置中心）
     */
    @Value("${jwt.secret-key:exam-system-jwt-secret-key-2024-very-secure-string-32chars}")
    private String secretKey;

    /**
     * Token 有效期（从配置文件读取，默认1小时）
     */
    @Value("${jwt.expiration:3600000}")
    private long expirationTime;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId    用户ID
     * @param userName  用户名
     * @param tableName 表名
     * @param role      角色
     * @return JWT Token
     */
    public String generateToken(Integer userId, String userName, String tableName, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
        claims.put("tableName", tableName);
        claims.put("role", role);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT Token
     * @return Claims（包含用户信息）
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否过期
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Integer getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (Integer) claims.get("userId") : null;
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUserName(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("userName") : null;
    }

    /**
     * 从 Token 中获取表名
     */
    public String getTableName(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("tableName") : null;
    }

    /**
     * 从 Token 中获取角色
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 获取 Token 过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getExpiration() : null;
    }
}
