package ru.xgodness.exception;

public class BindRequestAlreadyExistsException extends ApplicationException {
    public BindRequestAlreadyExistsException(String specialistUsername) {
        super("Вы уже отправляли заявку, чтобы стать клиентом специалиста %s".formatted(specialistUsername));
    }
}
