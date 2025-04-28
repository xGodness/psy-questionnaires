package ru.xgodness.security.util;

import org.springframework.security.core.context.SecurityContextHolder;
import ru.xgodness.endpoint.user.model.Role;

public class SecurityContextUtils {

    public static String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static Role getAuthenticatedRole() {
        return (Role) SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList().get(0);
    }

}
