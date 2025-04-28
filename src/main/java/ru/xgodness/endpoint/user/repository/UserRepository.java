package ru.xgodness.endpoint.user.repository;

import org.springframework.stereotype.Repository;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.model.User;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

@Repository
public class UserRepository extends JdbcRepository {

    public UserRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public Optional<User> findByUsername(String username) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM app_user WHERE username = ?;"
        )) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            return mapToUserOptional(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public Optional<User> findByUsernameAndRole(String username, Role role) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM app_user WHERE username = ? AND role = ?;"
        )) {
            statement.setString(1, username);
            statement.setObject(2, role, Types.OTHER);
            ResultSet rs = statement.executeQuery();

            return mapToUserOptional(rs);

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean save(User user) {
        try (var statement = super.getConnection().prepareStatement(
                "INSERT INTO app_user (username, passhash, salt, role) VALUES (?, ?, ?, ?);"
        )) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasshash());
            statement.setString(3, user.getSalt());
            statement.setObject(4, user.getRole().getAuthority(), Types.OTHER);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsByUsername(String username) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM app_user WHERE username = ?;"
        )) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsByUsernameAndRole(String username, Role role) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT 1 FROM app_user WHERE username = ? AND role = ?;"
        )) {
            statement.setString(1, username);
            statement.setObject(2, role, Types.OTHER);
            ResultSet rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    private Optional<User> mapToUserOptional(ResultSet rs) throws SQLException {
        if (rs.next())
            return Optional.of(new User(
                    rs.getString("username"),
                    rs.getString("passhash"),
                    rs.getString("salt"),
                    Role.valueOfIgnoreCase(rs.getString("role"))
            ));
        return Optional.empty();
    }
}
