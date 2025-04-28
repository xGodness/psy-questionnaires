package ru.xgodness.endpoint.questionnaire.repository;

import org.springframework.stereotype.Repository;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireIdentifier;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QuestionnaireIdentifierRepository extends JdbcRepository {

    public QuestionnaireIdentifierRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public boolean existsById(long id) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM questionnaire WHERE id = ?;"
        )) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<QuestionnaireIdentifier> findAll() {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM questionnaire;"
        )) {
            ResultSet rs = statement.executeQuery();
            return mapToIdentifierList(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<QuestionnaireIdentifier> findAllWithDisplayNameLike(String pattern) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM questionnaire WHERE LOWER(display_name) LIKE '%%%s%%';".formatted(pattern.toLowerCase())
        )) {
            ResultSet rs = statement.executeQuery();
            return mapToIdentifierList(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<QuestionnaireIdentifier> findAllAssignedTo(String clientUsername) {
        try (var statement = super.getConnection().prepareStatement("""
                SELECT id, name, display_name
                FROM questionnaire INNER JOIN assignment ON questionnaire.id = assignment.questionnaire_id
                WHERE client_username = ?;
                """
        )) {
            statement.setString(1, clientUsername);
            ResultSet rs = statement.executeQuery();
            return mapToIdentifierList(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    private List<QuestionnaireIdentifier> mapToIdentifierList(ResultSet rs) throws SQLException {
        List<QuestionnaireIdentifier> result = new ArrayList<>();

        QuestionnaireIdentifier identifier = mapToIdentifier(rs);
        while (identifier != null) {
            result.add(identifier);
            identifier = mapToIdentifier(rs);
        }

        return result;
    }

    private QuestionnaireIdentifier mapToIdentifier(ResultSet rs) throws SQLException {
        if (rs.next())
            return new QuestionnaireIdentifier(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("display_name")
            );

        return null;
    }

}
