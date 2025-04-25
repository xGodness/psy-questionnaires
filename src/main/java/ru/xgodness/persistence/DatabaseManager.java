package ru.xgodness.persistence;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Log
@Component
public class DatabaseManager {

    private static final String PG_DSN = System.getenv("PG_DSN");

    @Getter
    private final Connection connection;
    private final Set<Connection> initializedConnections = new HashSet<>();

    public DatabaseManager() {
        connection = initializeConnection();
    }

    public Connection initializeConnection() {
        try {
            if (PG_DSN == null)
                throw new RuntimeException("PG_DSN env variable is not set");
            Connection newConnection = DriverManager.getConnection(PG_DSN);
            initializedConnections.add(newConnection);
            return newConnection;
        } catch (SQLException ex) {
            log.severe("Could not initialize database connection: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void executeQuery(String query) {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            log.severe("Error while executing SQL statement: %s".formatted(query));
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @PreDestroy
    private void shutdown() throws SQLException {
        for (var connection : initializedConnections)
            connection.close();
    }
}
