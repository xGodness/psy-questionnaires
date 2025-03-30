package ru.xgodness.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.xgodness.persistence.sql.SQLBuilder;
import ru.xgodness.persistence.sql.SQLTemplateProvider;
import ru.xgodness.questionnaire.QuestionnaireProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Log
@Component
public class ConnectionManager {

    @Getter
    private final Connection connection;

    private final boolean isDropTablesOnShutdown;

    // TODO: remove
    public static void stub() {
    }

    public ConnectionManager(
            @Value("${development.drop-tables-on-shutdown}") String isDropTablesOnShutdown,
            @Value("${persistence.jdbc.url}") String url,
            @Value("${persistence.jdbc.username}") String username,
            @Value("${persistence.jdbc.password}") String password
    ) {
        this.isDropTablesOnShutdown = isDropTablesOnShutdown.equals("true");

        try {
            connection = DriverManager.getConnection(url, username, password);
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
            throw new RuntimeException();
        }
    }

    @PostConstruct
    private void initializeSchema() {
        SQLTemplateProvider.getTemplateMap().values().forEach(
                template -> {
                    String sql = template.createStatement();
                    executeSql(sql);
                }
        );

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

            SQLTemplateProvider.getTemplateMap().values().forEach(
                    template -> {
                        String sql = template.dropStatement();
                        executeSql(sql);
                    }
            );
        }

        connection.close();
    }
}
