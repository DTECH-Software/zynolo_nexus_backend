package com.zynolo_nexus.auth_service.util;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String SESSION_ID_CLAIM = "sessionId";

    private final String secret = "ChangeThisSecretToSomethingLongerAndSafer123!";
    private final long accessTokenValidityMs = 1000 * 60 * 40;
    private final long refreshTokenValidityMs = 1000 * 60 * 60 * 24;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username) {
        return buildToken(username, null, null, accessTokenValidityMs, ACCESS_TOKEN_TYPE);
    }

    public String generateAccessToken(String username, Long companyId) {
        return buildToken(username, companyId, null, accessTokenValidityMs, ACCESS_TOKEN_TYPE);
    }

    public String generateAccessToken(String username, Long companyId, String sessionId) {
        return buildToken(username, companyId, sessionId, accessTokenValidityMs, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, null, null, refreshTokenValidityMs, REFRESH_TOKEN_TYPE);
    }

    public String generateRefreshToken(String username, Long companyId) {
        return buildToken(username, companyId, null, refreshTokenValidityMs, REFRESH_TOKEN_TYPE);
    }

    public String generateRefreshToken(String username, Long companyId, String sessionId) {
        return buildToken(username, companyId, sessionId, refreshTokenValidityMs, REFRESH_TOKEN_TYPE);
    }

    public long getRefreshTokenValidityMs() {
        return refreshTokenValidityMs;
    }

    private String buildToken(String subject, Long companyId, String sessionId, long validity, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        var builder = Jwts.builder()
                .setSubject(subject)
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(expiry);

        if (companyId != null) {
            builder.claim("companyId", companyId);
        }
        if (sessionId != null && !sessionId.isBlank()) {
            builder.claim(SESSION_ID_CLAIM, sessionId);
        }

        return builder
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getCompanyId(String token) {
        Object value = parseClaims(token).get("companyId");
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String getSessionId(String token) {
        return parseClaims(token).get(SESSION_ID_CLAIM, String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return ACCESS_TOKEN_TYPE.equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return REFRESH_TOKEN_TYPE.equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
