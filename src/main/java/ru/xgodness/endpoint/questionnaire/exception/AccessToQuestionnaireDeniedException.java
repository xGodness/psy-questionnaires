package ru.xgodness.endpoint.questionnaire.exception;

import ru.xgodness.exception.ApplicationException;

public class AccessToQuestionnaireDeniedException extends ApplicationException {
    public AccessToQuestionnaireDeniedException(long questionnaireId) {
        super("Вам не был назначен опросник с идентификатором %d, обратитесь к своему специалисту, чтобы получить доступ".formatted(questionnaireId));
    }
}
