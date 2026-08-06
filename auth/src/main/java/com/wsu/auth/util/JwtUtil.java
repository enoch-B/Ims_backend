package com.wsu.auth.util;


import com.wsu.core.model.entities.Permission;
import com.wsu.core.model.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(User user) {
        List<String> permissionNames = user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .toList();

        return Jwts.builder()
                .subject(user.getUserName())
                .claim("role", user.getRole().getName())
                .claim("permissions", permissionNames)
                .claim("userId", user.getId())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUserName())
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generatePasswordChangeToken(String institutionalId) {
        return Jwts.builder()
                .subject(institutionalId)
                .claim("type", "password_change")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300000))
                .signWith(key())
                .compact();
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String institutionalId) {
        return extractSubject(token).equals(institutionalId) && !isExpired(token);
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
    }
}