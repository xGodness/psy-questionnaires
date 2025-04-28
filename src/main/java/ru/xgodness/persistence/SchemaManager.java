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
    private final QuestionnaireTemplateBuilder templateBuilder;
    private final DynamicMigrationDDLBuilder dynamicMigrationDDLBuilder;
    private final StaticMigrationDDLProvider staticMigrationDDLProvider;
    private final boolean isDropTablesOnShutdown;

    public SchemaManager(
            DatabaseManager databaseManager,
            QuestionnaireRegistry questionnaireRegistry,
            QuestionnaireTemplateBuilder templateBuilder,
            DynamicMigrationDDLBuilder dynamicMigrationDDLBuilder,
            StaticMigrationDDLProvider staticMigrationDDLProvider,
            @Value("${development.drop-tables-on-shutdown}") String isDropTablesOnShutdown
    ) {
        this.databaseManager = databaseManager;
        this.questionnaireRegistry = questionnaireRegistry;
        this.templateBuilder = templateBuilder;
        this.dynamicMigrationDDLBuilder = dynamicMigrationDDLBuilder;
        this.staticMigrationDDLProvider = staticMigrationDDLProvider;
        this.isDropTablesOnShutdown = Boolean.parseBoolean(isDropTablesOnShutdown);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void populate() {
        staticMigrationDDLProvider.getInitQueries().forEach(databaseManager::executeQuery);
        templateBuilder.getTemplates().forEach(questionnaireRegistry::register);
    }

    @PreDestroy
    private void shutdown() {
        if (isDropTablesOnShutdown) {
            questionnaireRegistry.getAllQuestionnaires().forEach(questionnaire -> {
                String dropQuery = dynamicMigrationDDLBuilder.buildDropTableForQuestionnaire(questionnaire);
                databaseManager.executeQuery(dropQuery);
            });

            databaseManager.executeQuery(staticMigrationDDLProvider.getDropAllQuery());
        }
    }

}
