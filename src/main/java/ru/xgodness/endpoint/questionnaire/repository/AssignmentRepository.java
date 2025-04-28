package ru.xgodness.endpoint.questionnaire.repository;

import org.springframework.stereotype.Repository;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class AssignmentRepository extends JdbcRepository {

    public AssignmentRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public boolean save(String clientUsername, long questionnaireId) {
        try (var statement = super.getConnection().prepareStatement(
                "INSERT INTO assignment (client_username, questionnaire_id) VALUES (?, ?);"
        )) {
            statement.setString(1, clientUsername);
            statement.setLong(2, questionnaireId);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean delete(String clientUsername, long questionnaireId) {
        try {
            return deleteTransactional(super.getConnection(), clientUsername, questionnaireId);
        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean deleteTransactional(Connection transactionConnection, String clientUsername, long questionnaireId) throws SQLException {
        try (var statement = transactionConnection.prepareStatement(
                "DELETE FROM assignment WHERE client_username = ? AND questionnaire_id = ?;"
        )) {
            statement.setString(1, clientUsername);
            statement.setLong(2, questionnaireId);
            return 1 == statement.executeUpdate();
        }
    }

    public boolean existsByClientUsernameAndQuestionnaireId(String clientUsername, long questionnaireId) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM assignment WHERE client_username = ? AND questionnaire_id = ?;"
        )) {
            statement.setString(1, clientUsername);
            statement.setLong(2, questionnaireId);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

}
