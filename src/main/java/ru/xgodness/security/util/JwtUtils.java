package ru.xgodness.security.util;

import io.jsonwebtoken.Claims;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.security.JwtAuthentication;

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
