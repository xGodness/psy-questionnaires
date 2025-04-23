package ru.xgodness.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.user.model.User;
import ru.xgodness.exception.AuthException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Log
@Component
public class JwtProvider {

    private final static String ISSUER = "ru.xgodness";
    private final static int REFRESH_EXPIRE_IN_DAYS = 30;
    private final static int ACCESS_EXPIRE_IN_MINUTES = 5;

    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;

    public JwtProvider() {
        String jwtAccessSecret = System.getenv("JWT_SECRET_ACCESS");
        String jwtRefreshSecret = System.getenv("JWT_SECRET_REFRESH");
        if (jwtAccessSecret == null || jwtRefreshSecret == null)
            throw new RuntimeException("Some of JWT secrets are not set as env variables");

        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    public String generateAccessToken(@NonNull User user) {
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(ACCESS_EXPIRE_IN_MINUTES)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return generateToken(user, expiration, jwtAccessSecret);
    }

    public String generateRefreshToken(@NonNull User user) {
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusDays(REFRESH_EXPIRE_IN_DAYS)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return generateToken(user, expiration, jwtRefreshSecret);
    }

    private String generateToken(User user, Date expiration, SecretKey secret) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .expiration(expiration)
                .signWith(secret)
                .compact();
    }

    public void validateAccessToken(String accessToken) {
        validateToken(accessToken, jwtAccessSecret);
    }

    public void validateRefreshToken(String refreshToken) {
        validateToken(refreshToken, jwtRefreshSecret);
    }

    private void validateToken(String token, SecretKey secret) {
        if (token == null)
            throw new AuthException("JWT must not be null");

        try {
            Jwts.parser()
                    .requireIssuer(ISSUER)
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException ex) {
            log.warning(ex.getMessage());

            String message;
            if (ex instanceof ExpiredJwtException)
                message = "JWT expired";
            else
                message = "Invalid JWT";
            throw new AuthException(message);
        }
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
