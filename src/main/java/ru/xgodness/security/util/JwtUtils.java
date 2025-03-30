package ru.xgodness.security.util;

import io.jsonwebtoken.Claims;
import ru.xgodness.security.JwtAuthentication;
import ru.xgodness.user.model.Role;

public class JwtUtils {
    public static JwtAuthentication generateAuthentication(Claims claims) {
        JwtAuthentication authentication = new JwtAuthentication();
        authentication.setRole(extractRoleFromClaims(claims));
        authentication.setUsername(claims.getSubject());
        return authentication;
    }

    private static Role extractRoleFromClaims(Claims claims) {
        return Role.valueOf(
                claims.get("role", String.class).toUpperCase()
        );
    }
}
