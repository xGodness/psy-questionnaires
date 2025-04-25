package ru.xgodness.persistence;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.questionnaire.dto.EvaluationQuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.dto.SelectionQuestionnaireTemplate;
import ru.xgodness.endpoint.questionnaire.model.EvaluationQuestionnaire;
import ru.xgodness.endpoint.questionnaire.model.Questionnaire;
import ru.xgodness.endpoint.questionnaire.model.SelectionQuestionnaire;

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

    private static final String INSERT_INTO_QUESTIONNAIRE_TABLE_QUERY = "INSERT INTO questionnaire (name, display_name) VALUES (?, ?) RETURNING id;";

    private final Connection transactionConnection;

    private final Map<Long, Questionnaire> registeredQuestionnaires = new HashMap<>();

    @Autowired
    public QuestionnaireRegistry(DatabaseManager databaseManager) throws SQLException {
        transactionConnection = databaseManager.initializeConnection();
        transactionConnection.setAutoCommit(false);
    }

    public void register(QuestionnaireTemplate template) {
        long id;

        try {
            PreparedStatement statement = transactionConnection.prepareStatement(INSERT_INTO_QUESTIONNAIRE_TABLE_QUERY);
            statement.setString(1, template.getName());
            statement.setString(2, template.getDisplayName());
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
    }

    public Questionnaire getQuestionnaire(long id) {
        return registeredQuestionnaires.get(id);
    }

    public Collection<Questionnaire> getAllQuestionnaires() {
        return registeredQuestionnaires.values();
    }

}
