package ru.xgodness.persistence;

import ru.xgodness.endpoint.questionnaire.model.Questionnaire;

import java.util.StringJoiner;

public class SQLBuilder {
    public static String buildCreateStatement(Questionnaire questionnaire) {
        StringJoiner joiner = new StringJoiner(", ");

        joiner.add(("CREATE TABLE IF NOT EXISTS %s (" +
                "id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "client_username varchar(32) REFERENCES app_user (username) ON DELETE CASCADE NOT NULL, " +
                "is_completed boolean DEFAULT false, " +
                "completion_date TIMESTAMP, " +
                "result_sum integer")
                .formatted(questionnaire.getName()));

        int questionCount = questionnaire.getQuestionCount();
        int answerCount = questionnaire.getAnswerCount();

        for (int i = 1; i <= questionCount; i++) {
            joiner.add("q%d integer CHECK (q%d >= 0 AND q%d < %d)".formatted(i, i, i, answerCount));
        }

        // TODO: trigger for checking user role ?
        return joiner + ");";
    }

    public static String buildDropStatement(Questionnaire questionnaire) {
        return "DROP TABLE IF EXISTS %s CASCADE;".formatted(questionnaire.getName());
    }

    public static String buildUpdateAnswerStatement(Questionnaire questionnaire, long recordId, int questionNumber, int answer) {
        if (questionNumber < 1 || questionNumber > questionnaire.getQuestionCount())
            throw new IllegalArgumentException("Invalid question number");
        if (answer < 1 || answer > questionnaire.getAnswerCount())
            throw new IllegalArgumentException("Invalid answer option");

        return "UPDATE %s SET q%d = %d WHERE id = %d;"
                .formatted(questionnaire.getName(), questionNumber, answer, recordId);
    }

    public static String buildInsertRecordStatement(Questionnaire questionnaire, String clientLogin) {
        return "INSERT INTO %s (client_username) VALUES %s;".formatted(questionnaire.getName(), clientLogin);
    }
}
