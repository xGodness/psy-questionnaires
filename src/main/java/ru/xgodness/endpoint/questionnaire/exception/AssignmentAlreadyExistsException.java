package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class AssignmentAlreadyExistsException extends ApplicationException {
    public AssignmentAlreadyExistsException(String clientUsername) {
        super("Вы уже назначили клиенту %s прохождение этого опросника".formatted(clientUsername));
    }
}
