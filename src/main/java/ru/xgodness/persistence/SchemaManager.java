package ru.xgodness.persistence;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.questionnaire.template.QuestionnaireTemplateBuilder;

@Component
public class SchemaManager {

    private final DatabaseManager databaseManager;
    private final QuestionnaireRegistry questionnaireRegistry;
    private final boolean isDropTablesOnShutdown;

    public SchemaManager(
            DatabaseManager databaseManager,
            QuestionnaireRegistry questionnaireRegistry,
            @Value("${development.drop-tables-on-shutdown}") String isDropTablesOnShutdown
    ) {
        this.databaseManager = databaseManager;
        this.questionnaireRegistry = questionnaireRegistry;
        this.isDropTablesOnShutdown = Boolean.parseBoolean(isDropTablesOnShutdown);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void populate() {
        StaticMigrationDDLProvider.getInitQueries().forEach(databaseManager::executeQuery);
        QuestionnaireTemplateBuilder.getTemplates().forEach(questionnaireRegistry::register);
    }

    @PreDestroy
    private void shutdown() {
        if (isDropTablesOnShutdown) {
            questionnaireRegistry.getAllQuestionnaires().forEach(questionnaire -> {
                String dropQuery = DynamicMigrationDDLBuilder.buildDropTableForQuestionnaire(questionnaire);
                databaseManager.executeQuery(dropQuery);
            });

            databaseManager.executeQuery(StaticMigrationDDLProvider.getDropAllQuery());
        }
    }

}
