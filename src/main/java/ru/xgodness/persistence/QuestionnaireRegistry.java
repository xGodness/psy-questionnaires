package ru.xgodness.persistence;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireForm;
import ru.xgodness.endpoint.questionnaire.model.EvaluationQuestionnaire;
import ru.xgodness.endpoint.questionnaire.model.Questionnaire;
import ru.xgodness.endpoint.questionnaire.model.SelectionQuestionnaire;
import ru.xgodness.endpoint.questionnaire.template.EvaluationQuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.template.QuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.template.SelectionQuestionnaireTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log
@Component
public class QuestionnaireRegistry {

    private static final String INSERT_INTO_QUESTIONNAIRE_TABLE_QUERY = """
            WITH
            sel AS (
                SELECT id
                FROM questionnaire
                WHERE name = ?
            ),
            ins AS (
                INSERT INTO questionnaire (name, display_name)
                SELECT ?, ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM sel
                )
                RETURNING id
            )
            SELECT id FROM ins UNION ALL SELECT id FROM sel;
            """;

    private final Connection transactionConnection;

    private final Map<Long, Questionnaire> registeredQuestionnaires = new HashMap<>();
    private final Map<Long, QuestionnaireForm> questionnaireForms = new HashMap<>();

    public QuestionnaireRegistry(DatabaseManager databaseManager) throws SQLException {
        transactionConnection = databaseManager.initializeConnection();
        transactionConnection.setAutoCommit(false);
    }

    public void register(QuestionnaireTemplate template) {
        long id;

        try {
            PreparedStatement statement = transactionConnection.prepareStatement(INSERT_INTO_QUESTIONNAIRE_TABLE_QUERY);
            statement.setString(1, template.getName());
            statement.setString(2, template.getName());
            statement.setString(3, template.getDisplayName());
            ResultSet rs = statement.executeQuery();

            String ddlSql = DynamicMigrationDDLBuilder.buildCreateTableForQuestionnaire(template).formatted(template.getName());

            statement = transactionConnection.prepareStatement(ddlSql);
            statement.executeUpdate();

            transactionConnection.commit();

            rs.next();
            id = rs.getLong("id");

        } catch (SQLException ex) {
            log.severe("Error while executing SQL: " + ex.getMessage());
            throw new RuntimeException(ex);
        }

        Questionnaire questionnaire = switch (template.getType()) {
            case SELECTION -> new SelectionQuestionnaire(id, (SelectionQuestionnaireTemplate) template);
            case EVALUATION -> new EvaluationQuestionnaire(id, (EvaluationQuestionnaireTemplate) template);
        };

        registeredQuestionnaires.put(questionnaire.getId(), questionnaire);
        questionnaireForms.put(questionnaire.getId(), new QuestionnaireForm(questionnaire));
    }

    public Questionnaire getQuestionnaire(long id) {
        return registeredQuestionnaires.get(id);
    }

    public Collection<Questionnaire> getAllQuestionnaires() {
        return registeredQuestionnaires.values();
    }

    public QuestionnaireForm getQuestionnaireForm(long id) {
        return questionnaireForms.get(id);
    }

}
