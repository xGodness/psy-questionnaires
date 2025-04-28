package ru.xgodness.endpoint.questionnaire.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireAnswersState;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireCompletionState;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class QuestionnaireCompletionRepository extends JdbcRepository {

    private final String questionnaireResultTableSuffix;

    public QuestionnaireCompletionRepository(
            DatabaseManager databaseManager,
            @Value("${application.questionnaire.result-table-suffix}") String questionnaireResultTableSuffix
    ) {
        super(databaseManager);
        this.questionnaireResultTableSuffix = questionnaireResultTableSuffix;
    }

    public QuestionnaireAnswersState saveTransactional(Connection transactionConnection, String clientUsername, String questionnaireName, int questionCount) throws SQLException {
        try (var statement = transactionConnection.prepareStatement(
                "INSERT INTO %s (client_username) VALUES (?) RETURNING *;"
                        .formatted(questionnaireName + questionnaireResultTableSuffix)
        )) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();

            return mapToAnswersState(rs, questionCount);
        }
    }

    public QuestionnaireAnswersState update(String clientUsername, String questionnaireName, int questionCount, Map<Integer, Integer> questionUpdates) {
        String query = "UPDATE %s SET ".formatted(questionnaireName + questionnaireResultTableSuffix);

        StringJoiner updates = new StringJoiner(", ");
        questionUpdates.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .forEach(entry -> updates.add("q%d = %d".formatted(entry.getKey(), entry.getValue())));

        query += updates + " WHERE client_username = ? AND is_completed = false RETURNING *;";

        try (var statement = super.getConnection().prepareStatement(query)) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();

            return mapToAnswersState(rs, questionCount);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public Optional<QuestionnaireCompletionState> findNotCompletedByClientUsername(String clientUsername, String questionnaireName, int questionCount) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM %s WHERE client_username = ? AND is_completed = false;"
                        .formatted(questionnaireName + questionnaireResultTableSuffix)
        )) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();

            return mapToCompletionStateOptional(rs, questionCount);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean complete(String clientUsername, String questionnaireName, int resultSum, String resultInterpretation) {
        try (var statement = super.getConnection().prepareStatement(
                "UPDATE %s SET is_completed = true, completion_date = now(), result_sum = ?, result_interpretation = ? WHERE client_username = ? AND is_completed = false;"
                        .formatted(questionnaireName + questionnaireResultTableSuffix)
        )) {
            statement.setInt(1, resultSum);
            statement.setString(2, resultInterpretation);
            statement.setString(3, clientUsername);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsNotCompletedByClientUsername(String clientUsername, String questionnaireName) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM %s WHERE client_username = ? AND is_completed = false;"
                        .formatted(questionnaireName + questionnaireResultTableSuffix)
        )) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();

            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<QuestionnaireCompletionState> findAllCompletedByClientUsername(String clientUsername, String questionnaireName, int questionCount) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM %s WHERE client_username = ? AND is_completed = true ORDER BY completion_date DESC;"
                        .formatted(questionnaireName + questionnaireResultTableSuffix)
        )) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();

            return mapToCompletionStateList(rs, questionCount);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    private Optional<QuestionnaireCompletionState> mapToCompletionStateOptional(ResultSet rs, int questionCount) throws SQLException {
        QuestionnaireCompletionState state = mapToCompletionState(rs, questionCount);
        return state == null ? Optional.empty() : Optional.of(state);
    }

    private QuestionnaireAnswersState mapToAnswersState(ResultSet rs, int questionCount) throws SQLException {
        return new QuestionnaireAnswersState(mapToAnswers(rs, questionCount));
    }

    private Map<Integer, Integer> mapToAnswers(ResultSet rs, int questionCount) throws SQLException {
        Map<Integer, Integer> answers = new HashMap<>();
        if (rs.next()) {
            for (var i = 1; i <= questionCount; i++)
                answers.put(i, rs.getInt("q%d".formatted(i)));
        }
        return answers;
    }

    private List<QuestionnaireCompletionState> mapToCompletionStateList(ResultSet rs, int questionCount) throws SQLException {
        List<QuestionnaireCompletionState> result = new ArrayList<>();
        QuestionnaireCompletionState state = mapToCompletionState(rs, questionCount);
        while (state != null) {
            result.add(state);
            state = mapToCompletionState(rs, questionCount);
        }
        return result;
    }

    private QuestionnaireCompletionState mapToCompletionState(ResultSet rs, int questionCount) throws SQLException {
        Map<Integer, Integer> answers = mapToAnswers(rs, questionCount);
        if (answers.isEmpty()) return null;

        for (var i = 1; i <= questionCount; i++)
            answers.put(i, rs.getInt("q%d".formatted(i)));

        return new QuestionnaireCompletionState(
                rs.getTimestamp("completion_date"),
                rs.getInt("result_sum"),
                rs.getString("result_interpretation"),
                answers
        );
    }

}
