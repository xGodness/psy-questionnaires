package ru.xgodness.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConnectionParams {
    private String url;
    private String username;
    private String password;
}
