package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class QuestionnaireNotFoundException extends ApplicationException {
    public QuestionnaireNotFoundException(String name) {
        super("Опросник с названием %s не найден".formatted(name));
    }

    public QuestionnaireNotFoundException(long id) {
        super("Опросник с идентификатором %d не найден".formatted(id));
    }
}
