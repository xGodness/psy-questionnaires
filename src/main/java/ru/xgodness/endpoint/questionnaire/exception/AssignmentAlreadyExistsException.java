package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class AssignmentAlreadyExistsException extends ApplicationException {
    public AssignmentAlreadyExistsException(String clientUsername) {
        super("Клиенту %s уже назначено заполнение этого опросника".formatted(clientUsername));
    }
}
