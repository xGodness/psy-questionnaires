package ru.xgodness.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.xgodness.exception.AuthException;
import ru.xgodness.user.model.User;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Log
@Component
public class JwtProvider {

    private final String ISSUER = "ru.xgodness";

    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;

    public JwtProvider(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}") String jwtRefreshSecret
    ) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    // TODO: check that user isn't null
    public String generateAccessToken(User user) {
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(5)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return generateToken(user, expiration, jwtAccessSecret);
    }

    // TODO: check that user isn't null
    public String generateRefreshToken(User user) {
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusDays(30)
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

    // TODO: check token isn't null
    public void validateAccessToken(String accessToken) {
        validateToken(accessToken, jwtAccessSecret);
    }

    // TODO: check token isn't null
    public void validateRefreshToken(String refreshToken) {
        validateToken(refreshToken, jwtRefreshSecret);
    }

    // TODO: check token isn't null
    private void validateToken(String token, SecretKey secret) {
        // TODO: is it possible to check
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

    // TODO: check token isn't null
    public Claims getAccessClaims(String token) {
        return getClaims(token, jwtAccessSecret);
    }

    // TODO: check token isn't null
    public Claims getRefreshClaims(String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    // TODO: check token isn't null
    private Claims getClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
