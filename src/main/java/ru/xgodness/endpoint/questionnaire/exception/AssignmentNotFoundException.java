package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class AssignmentNotFoundException extends ApplicationException {
    public AssignmentNotFoundException(String clientUsername) {
        super("Клиенту %s не было назначено заполнение этого опросника".formatted(clientUsername));
    }
}
