package com.ocbs.ocbs.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    /**
     * JwtService depends on JwtConfig.
     * JwtConfig reads from application.properties / env vars.
     *env vars / .env
     *       ↓
     * application.properties
     *       ↓
     * JwtConfig          ← reads JWT properties
     *       ↓
     * JwtService         ← uses JwtConfig to sign/verify tokens
     *       ↓
     * JwtAuthFilter      ← uses JwtService to validate requests
     *       ↓
     * AppConfig          ← registers JwtAuthFilter into security chain
     */

    @Value("${oscbs.jwt.secret}")
    private String secret;

    @Value("${oscbs.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${oscbs.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;


    private SecretKey getSigningKey()
    {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateRefreshToken(UUID userId) {
        log.debug("Generating refresh token for user: {}", userId);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean notExpired = !claims.getExpiration().before(new Date());
            boolean isAccessToken = "ACCESS".equals(claims.get("type"));
            return notExpired && isAccessToken;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String generateAccessToken(UUID userId, String username, String roles) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return (String) extractClaims(token).get("username");
    }

    public String extractRoles(String token) {
        return (String) extractClaims(token).get("roles");
    }
}
