package com.zhm.springboot.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.access-expire:3600000}")
    private long accessExpire;

    @Value("${jwt.refresh-expire:604800000}")
    private long refreshExpire;

    private Key secretKey;

    @PostConstruct
    public void init() {
        if (secretString == null || secretString.isBlank()) {
            throw new IllegalStateException(
                "JWT 密钥未配置，请设置环境变量 JWT_SECRET（至少64字节）");
        }
        if (secretString.getBytes().length < 64) {
            throw new IllegalStateException(
                "JWT 密钥长度不足，HS512 要求至少64字节，当前：" + secretString.getBytes().length + " 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes());
        log.info("JWT 工具初始化完成，accessExpire={}ms, refreshExpire={}ms", accessExpire, refreshExpire);
    }

    public String createToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpire))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Long parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 解析失败（签名无效或格式错误）: {}", e.getMessage());
            return null;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public long getRefreshExpire() {
        return refreshExpire;
    }
}
