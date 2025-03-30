package ru.xgodness.endpoint.user.dto;

import lombok.Getter;
import ru.xgodness.user.model.Role;

@Getter
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
