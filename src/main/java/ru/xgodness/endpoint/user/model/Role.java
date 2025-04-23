package ru.xgodness.endpoint.user.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    CLIENT("CLIENT"),
    SPECIALIST("SPECIALIST");

    private final String value;

    @Override
    public String getAuthority() {
        return value;
    }

    public static Role valueOfIgnoreCase(String name) {
        return valueOf(name.toUpperCase());
    }
}
