package ru.xgodness.endpoint.user.exception;

import ru.xgodness.exception.ApplicationException;

public class BindNotFoundException extends ApplicationException {
    public BindNotFoundException(String clientUsername) {
        super("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(clientUsername));
    }
}
