package ru.xgodness.persistence;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.persistence.sql.SQLBuilder;
import ru.xgodness.persistence.sql.SQLTemplateProvider;
import ru.xgodness.questionnaire.QuestionnaireProvider;
import ru.xgodness.util.PropertiesProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Log
@Component
public class ConnectionManager {
    private final Connection connection;

    public static void stub() {
    }

    public ConnectionManager() {
        var params = PropertiesProvider.getConnectionParams();

        try {
            connection = DriverManager.getConnection(params.getUrl(), params.getUsername(), params.getPassword());
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
    private void initializeTables() {
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
        if (PropertiesProvider.dropTablesOnShutdown()) {
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
