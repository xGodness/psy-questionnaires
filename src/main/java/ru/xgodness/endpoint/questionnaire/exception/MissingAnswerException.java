package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class MissingAnswerException extends ApplicationException {
    public MissingAnswerException() {
        super("Вы еще не ответили на все вопросы");
    }
}
