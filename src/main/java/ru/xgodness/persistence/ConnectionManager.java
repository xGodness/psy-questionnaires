package ru.xgodness.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.questionnaire.QuestionnaireProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Log
@Component
public class ConnectionManager {

    @Getter
    private final Connection connection;

    private final boolean isDropTablesOnShutdown;

    public ConnectionManager(
            @Value("${development.drop-tables-on-shutdown}") String isDropTablesOnShutdown
    ) {
        this.isDropTablesOnShutdown = Boolean.parseBoolean(isDropTablesOnShutdown);

        try {
            String dsn = System.getenv("PG_DSN");
            if (dsn == null)
                throw new RuntimeException("PG_DSN env variable is not set");
            connection = DriverManager.getConnection(dsn);
        } catch (SQLException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void executeSql(String sql) {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            log.severe("Error while executing SQL statement: %s".formatted(sql));
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @PostConstruct
    private void initializeSchema() {
        MigrationProvider.getInitQueries().forEach(this::executeSql);

        QuestionnaireProvider.getQuestionnaireMap().values().forEach(
                questionnaire -> {
                    String sql = SQLBuilder.buildCreateStatement(questionnaire);
                    executeSql(sql);
                }
        );
    }

    @PreDestroy
    private void shutdown() throws SQLException {
        if (isDropTablesOnShutdown) {
            QuestionnaireProvider.getQuestionnaireMap().values().forEach((
                    questionnaire -> {
                        String sql = SQLBuilder.buildDropStatement(questionnaire);
                        executeSql(sql);
                    }
            ));

            executeSql(MigrationProvider.getDropAllQuery());
        }

        connection.close();
    }
}
