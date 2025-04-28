package ru.xgodness.persistence;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Log
public class StaticMigrationDDLProvider {
    private static final String MIGRATIONS_DIR_PATH = "src/main/resources/migrations";

    @SneakyThrows
    public static List<String> getInitQueries() {
        try (Stream<Path> paths = Files.walk(Paths.get(MIGRATIONS_DIR_PATH + "/init"))) {
            List<Path> filteredPaths = paths
                    .sorted()
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .toList();

            List<String> initQueries = new ArrayList<>();
            for (Path path : filteredPaths)
                initQueries.add(Files.readString(path));

            return initQueries;
        }
    }

    @SneakyThrows
    public static String getDropAllQuery() {
        return Files.readString(Path.of(MIGRATIONS_DIR_PATH + "/drop/drop_all_static.sql"));
    }
}
