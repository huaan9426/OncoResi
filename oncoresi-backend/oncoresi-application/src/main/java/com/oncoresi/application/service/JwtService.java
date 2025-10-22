package com.oncoresi.application.service;

import com.oncoresi.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JWT服务
 */
@Service
public class JwtService {

    @Value("${jwt.secret:oncoresi-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expiration;

    /**
     * 生成Token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoleCodes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        Object roles = getClaimsFromToken(token).get("roles");
        if (roles instanceof Set) {
            return (Set<String>) roles;
        }
        return Set.of();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析Token获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
