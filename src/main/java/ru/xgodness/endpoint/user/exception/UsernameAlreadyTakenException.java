package ru.xgodness.endpoint.user.exception;

import ru.xgodness.exception.ApplicationException;

public class UsernameAlreadyTakenException extends ApplicationException {
    public UsernameAlreadyTakenException(String username) {
        super("Пользователь с именем %s уже существует".formatted(username));
    }
}
