package ru.xgodness.endpoint.user.exception;

import ru.xgodness.exception.ApplicationException;

public class UserNotFoundException extends ApplicationException {
    public UserNotFoundException(String username) {
        super("Пользователь с именем %s не найден".formatted(username));
    }
}
