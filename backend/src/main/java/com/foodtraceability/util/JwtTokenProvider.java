package com.foodtraceability.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.foodtraceability.exception.BusinessException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:food-traceability-system-jwt-secret-key-2026}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenByUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        
        } catch (SecurityException ex) {
            log.error("[JWT 验证] 无效的 JWT 签名");
            throw new BusinessException("无效的 JWT 签名");
        } catch (MalformedJwtException ex) {
            log.error("[JWT 验证] 无效的 JWT 格式");
            throw new BusinessException("无效的 JWT 格式");
        } catch (ExpiredJwtException ex) {
            log.error("[JWT 验证] JWT 已过期");
            throw new BusinessException("JWT 已过期，请重新登录");
        } catch (UnsupportedJwtException ex) {
            log.error("[JWT 验证] 不支持的 JWT");
            throw new BusinessException("不支持的 JWT");
        } catch (IllegalArgumentException ex) {
            log.error("[JWT 验证] JWT claims 字符串为空");
            throw new BusinessException("JWT claims 字符串为空");
        }

        return true;
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }
}
