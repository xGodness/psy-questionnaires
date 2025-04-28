package ru.xgodness.endpoint.user.exception;

import ru.xgodness.exception.ApplicationException;

public class BindRequestAlreadyExistsException extends ApplicationException {
    public BindRequestAlreadyExistsException(String specialistUsername) {
        super("Вы уже отправляли заявку, чтобы стать клиентом специалиста %s".formatted(specialistUsername));
    }
}
