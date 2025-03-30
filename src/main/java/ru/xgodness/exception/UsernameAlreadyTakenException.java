package ru.xgodness.exception;

public class UsernameAlreadyTakenException extends ApplicationException {
    public UsernameAlreadyTakenException(String username) {
        super("Пользователь с именем %s уже существует".formatted(username));
    }
}
