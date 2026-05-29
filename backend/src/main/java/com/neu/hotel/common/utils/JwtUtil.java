package com.neu.hotel.common.utils;

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
 * JWT 令牌工具类
 *
 * <p>提供 JWT 令牌的生成、解析及 Claims 提取等功能。
 * 令牌中携带 userId、roleId、userType 等关键信息，
 * 用于用户认证和接口权限校验。
 *
 * <p>依赖配置（application.yml）：
 * <ul>
 *   <li>jwt.secret    - 签名密钥（至少 32 字符）</li>
 *   <li>jwt.expiration - 令牌有效期（毫秒）</li>
 * </ul>
 */
@Component
public class JwtUtil {

    /**
     * 签名密钥，从配置文件读取
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 令牌有效期（毫秒），从配置文件读取
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 构建签名密钥
     *
     * @return 根据 secret 生成的 HMAC-SHA 密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT 令牌
     *
     * <p>将用户名、用户ID、角色ID、用户类型等信息写入 Claims，
     * 并设置签发时间和过期时间。
     *
     * @param username 用户名（作为 JWT subject）
     * @param userId   用户ID
     * @param roleId   角色ID
     * @param userType 用户类型（如：1=后台用户，2=前台访客）
     * @return 签名的 JWT 字符串
     */
    public String generateToken(String username, Long userId, Long roleId, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleId", roleId);
        claims.put("userType", userType);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 JWT 令牌，提取用户名（subject）
     *
     * @param token JWT 令牌字符串
     * @return 用户名，解析失败时返回 null
     */
    public String parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 JWT 令牌，提取完整 Claims
     *
     * @param token JWT 令牌字符串
     * @return Claims 对象，包含所有声明信息
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中提取用户ID
     *
     * @param token JWT 令牌字符串
     * @return 用户ID，解析失败或不存在时返回 null
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中提取角色ID
     *
     * @param token JWT 令牌字符串
     * @return 角色ID，解析失败或不存在时返回 null
     */
    public Long getRoleIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return Long.valueOf(claims.get("roleId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中提取用户类型
     *
     * @param token JWT 令牌字符串
     * @return 用户类型，解析失败时默认返回 0
     */
    public Integer getUserTypeFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Object ut = claims.get("userType");
            return ut != null ? Integer.valueOf(ut.toString()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判断令牌是否已过期
     *
     * @param token JWT 令牌字符串
     * @return 已过期返回 true，未过期或解析失败返回 true（保守判断）
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
