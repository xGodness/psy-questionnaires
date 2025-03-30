package ru.xgodness.endpoint.user.dto;

import lombok.Getter;

@Getter
public class JwtRequest {
    private String username;
    private String password;
}
