package ru.xgodness.exception;

public class UserNotFoundException extends ApplicationException {
    public UserNotFoundException(String username) {
        super("Пользователь с именем %s не найден".formatted(username));
    }
}
