package ru.xgodness.endpoint.user.repository;

import org.springframework.stereotype.Repository;
import ru.xgodness.endpoint.user.dto.BindList;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BindRepository extends JdbcRepository {

    public BindRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public boolean createBindRequest(String clientUsername, String specialistUsername) {
        try (var statement = super.getConnection().prepareStatement(
                "INSERT INTO client_specialist (client_username, specialist_username) values (?, ?);"
        )) {
            statement.setString(1, clientUsername);
            statement.setString(2, specialistUsername);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean approveBindRequest(String clientUsername, String specialistUsername) {
        try (var statement = super.getConnection().prepareStatement(
                "UPDATE client_specialist SET is_approved = true WHERE client_username = ? AND specialist_username = ?;"
        )) {
            statement.setString(1, clientUsername);
            statement.setString(2, specialistUsername);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean discardBindRequest(String clientUsername, String specialistUsername) {
        try (var statement = super.getConnection().prepareStatement(
                "DELETE FROM client_specialist WHERE client_username = ? AND specialist_username = ? AND is_approved = false;"
        )) {
            statement.setString(1, clientUsername);
            statement.setString(2, specialistUsername);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public BindList findAllBySpecialistUsername(String specialistName) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT client_username, is_approved FROM client_specialist WHERE specialist_username = ?;"
        )) {
            statement.setString(1, specialistName);
            ResultSet rs = statement.executeQuery();

            List<String> approved = new ArrayList<>();
            List<String> pending = new ArrayList<>();

            boolean isApproved;
            String clientUsername;
            while (rs.next()) {
                isApproved = rs.getBoolean("is_approved");
                clientUsername = rs.getString("client_username");
                if (isApproved)
                    approved.add(clientUsername);
                else
                    pending.add(clientUsername);
            }

            return new BindList(pending, approved);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<String> findAllApprovedBySpecialistUsername(String specialistName) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT client_username FROM client_specialist WHERE specialist_username = ? AND is_approved = true;"
        )) {
            statement.setString(1, specialistName);
            ResultSet rs = statement.executeQuery();

            return mapToClientUsernameList(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public List<String> findAllPendingBySpecialistUsername(String specialistName) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT client_username FROM client_specialist WHERE specialist_username = ? AND is_approved = false;"
        )) {
            statement.setString(1, specialistName);
            ResultSet rs = statement.executeQuery();

            return mapToClientUsernameList(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsByClientUsernameAndSpecialistUsername(String clientUsername, String specialistUsername) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM client_specialist WHERE client_username = ? AND specialist_username = ?;"
        )) {
            statement.setString(1, clientUsername);
            statement.setString(2, specialistUsername);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsApprovedByClientUsernameAndSpecialistUsername(String clientUsername, String specialistUsername) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM client_specialist WHERE client_username = ? AND specialist_username = ? AND is_approved = true;"
        )) {
            statement.setString(1, clientUsername);
            statement.setString(2, specialistUsername);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    private List<String> mapToClientUsernameList(ResultSet rs) throws SQLException {
        List<String> result = new ArrayList<>();

        while (rs.next())
            result.add(rs.getString("client_username"));

        return result;
    }

}
