package ru.xgodness.endpoint.user.dto;

import lombok.Getter;

@Getter
public class RefreshJwtRequest {
    private String refreshToken;
}
