package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class CompletionNotStartedException extends ApplicationException {
    public CompletionNotStartedException(long questionnaireId) {
        super("Вы еще не начали заполнять опросник с идентификатором %d".formatted(questionnaireId));
    }
}
