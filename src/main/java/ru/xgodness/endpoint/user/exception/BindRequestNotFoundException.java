package ru.xgodness.endpoint.user.exception;

import ru.xgodness.exception.ApplicationException;

public class BindRequestNotFoundException extends ApplicationException {
    public BindRequestNotFoundException(String clientUsername) {
        super("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(clientUsername));
    }
}
