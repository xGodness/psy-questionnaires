package ru.xgodness.security.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtils {

    public static String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
