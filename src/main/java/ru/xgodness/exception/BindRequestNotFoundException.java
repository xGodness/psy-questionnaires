package ru.xgodness.exception;

public class BindRequestNotFoundException extends ApplicationException {
    public BindRequestNotFoundException(String clientUsername) {
        super("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(clientUsername));
    }
}
