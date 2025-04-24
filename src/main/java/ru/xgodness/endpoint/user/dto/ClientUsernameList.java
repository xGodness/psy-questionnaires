package ru.xgodness.endpoint.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClientUsernameList {
    private List<String> clients;
}
