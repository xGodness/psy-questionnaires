package ru.xgodness.endpoint.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BindList {
    private ClientUsernameList pending;
    private ClientUsernameList approved;

    public BindList(List<String> pending, List<String> approved) {
        this.pending = new ClientUsernameList(pending);
        this.approved = new ClientUsernameList(approved);
    }
}
