package ru.xgodness.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class User {
    private String username;
    private String passhash;
    private String salt;
    private Role role;
}
