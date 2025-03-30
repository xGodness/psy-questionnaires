package ru.xgodness.exception;

public class UsernameNotFoundException extends ApplicationException {
    public UsernameNotFoundException(String username) {
        super("Пользователь с именем %s не найден".formatted(username));
    }
}
