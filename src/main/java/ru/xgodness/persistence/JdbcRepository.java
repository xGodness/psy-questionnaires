package ru.xgodness.persistence;

import lombok.Getter;
import lombok.extern.java.Log;
import ru.xgodness.exception.SQLExecutionException;

import java.sql.Connection;
import java.sql.SQLException;

@Log
public abstract class JdbcRepository {
    @Getter
    private final Connection connection;

    protected JdbcRepository(DatabaseManager databaseManager) {
        this.connection = databaseManager.getConnection();
    }

    protected SQLExecutionException handleSQLException(SQLException ex) {
        log.severe("Error while executing SQL statement");
        log.severe(ex.getMessage());
        return new SQLExecutionException();
    }
}
