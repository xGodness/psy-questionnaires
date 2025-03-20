package ru.xgodness.util;

import lombok.Getter;
import lombok.extern.java.Log;
import ru.xgodness.persistence.ConnectionParams;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Log
public class PropertiesProvider {
    @Getter
    private static final String RESOURCES_PATH = requireNonNull(PropertiesProvider.class.getClassLoader().getResource("")).getPath();
    private static final String PROPERTIES_FILENAME = "application.properties";
    private static final Properties properties;

    static {
        String appPropertiesFilepath = RESOURCES_PATH + PROPERTIES_FILENAME;
        properties = loadProperties(appPropertiesFilepath);
    }

    private static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
        return properties;
    }

    public static ConnectionParams getConnectionParams() {
        return new ConnectionParams(
                properties.getProperty("persistence.jdbc.url"),
                properties.getProperty("persistence.jdbc.username"),
                properties.getProperty("persistence.jdbc.password")
        );
    }

    public static boolean dropTablesOnShutdown() {
        return properties.getProperty("development.drop-tables-on-shutdown").equals("1");
    }

}
