package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class AssignmentNotFoundException extends ApplicationException {
    public AssignmentNotFoundException(String clientUsername) {
        super("Вы не назначали клиенту %s прохождение этого опросника".formatted(clientUsername));
    }
}
