package ru.xgodness.persistence;

import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.model.Questionnaire;

import java.util.StringJoiner;

public class DynamicMigrationDDLBuilder {

    public static String buildCreateTableForQuestionnaire(QuestionnaireTemplate template) {
        StringJoiner joiner = new StringJoiner(", ");

        joiner.add(("CREATE TABLE IF NOT EXISTS %s (" +
                "id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "client_username varchar(32) REFERENCES app_user (username) ON DELETE CASCADE NOT NULL, " +
                "is_completed boolean DEFAULT false, " +
                "completion_date TIMESTAMP, " +
                "result_sum integer")
                .formatted(template.getName()));

        int questionCount = template.getQuestionCount();
        int answerCount = template.getAnswerCount();

        for (int i = 1; i <= questionCount; i++) {
            joiner.add("q%d integer CHECK (q%d >= 0 AND q%d < %d)".formatted(i, i, i, answerCount));
        }

        // TODO: trigger for checking user role ?
        return joiner + ");";
    }

    public static String buildDropTableForQuestionnaire(Questionnaire questionnaire) {
        return "DROP TABLE IF EXISTS %s CASCADE;".formatted(questionnaire.getName());
    }

}
