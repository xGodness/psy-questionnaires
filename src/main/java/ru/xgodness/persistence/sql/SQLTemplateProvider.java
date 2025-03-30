package ru.xgodness.persistence.sql;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.json.JSONObject;
import ru.xgodness.persistence.ConnectionManager;
import ru.xgodness.util.PropertiesProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@Log
public class SQLTemplateProvider {
    private static final String TEMPLATE_FILE_SUFFIX = ".sql.json";
    private static final String TEMPLATES_DIR_PATH = PropertiesProvider.getRESOURCES_PATH() + "sql/";
    /**
     * LinkedHashMap used to preserve order of insertions for proper SQL statements order of execution later
     *
     * @see ConnectionManager#initializeTables()
     */
    @Getter
    private static final Map<String, SQLTemplate> templateMap = new LinkedHashMap<>();

    static {
        try {
            initializeTemplates();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    // TODO: getNames, getTemplate (also for QuestionnaireProvider)

    @SneakyThrows
    private static void initializeTemplates() {
        try (Stream<Path> paths = Files.walk(Paths.get(TEMPLATES_DIR_PATH))) {
            paths
                    .sorted()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(TEMPLATE_FILE_SUFFIX))
                    .forEach(SQLTemplateProvider::mapTemplate);
        }
    }

    @SneakyThrows(IOException.class)
    private static void mapTemplate(Path templateFilePath) {
        String jsonString = Files.readString(templateFilePath);
        JSONObject rootObj = new JSONObject(jsonString);

        var name = rootObj.getString("name");
        var createStatement = rootObj.getString("create");
        var insertStatement = rootObj.getString("insert");
        var dropStatement = rootObj.getString("drop");

        templateMap.put(name, new SQLTemplate(createStatement, insertStatement, dropStatement));
    }
}
