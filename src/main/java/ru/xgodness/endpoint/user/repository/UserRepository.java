package ru.xgodness.endpoint.user.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.model.User;
import ru.xgodness.persistence.ConnectionManager;
import ru.xgodness.persistence.JdbcRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

@Repository
public class UserRepository extends JdbcRepository {

    @Autowired
    public UserRepository(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    // TODO: should I use @NonNull from lombok to generate checks here? or just validate externally?

    public Optional<User> findUserByUsername(String username) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM app_user WHERE username = ?;"
        )) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return Optional.of(new User(
                        rs.getString("username"),
                        rs.getString("passhash"),
                        rs.getString("salt"),
                        Role.valueOf(rs.getString("role"))
                ));
            return Optional.empty();
        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public void saveUser(User user) {
        try (var statement = super.getConnection().prepareStatement(
                "INSERT INTO app_user (username, passhash, salt, role) VALUES (?, ?, ?, ?);"
        )) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasshash());
            statement.setString(3, user.getSalt());
            statement.setObject(4, user.getRole().getAuthority(), Types.OTHER);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }

    public boolean existsByUsername(String username) {
        try (var statement = super.getConnection().prepareStatement(
                "SELECT * FROM app_user WHERE username = ?;"
        )) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw super.handleSQLException(ex);
        }
    }
}
