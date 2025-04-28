package ru.xgodness.persistence;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Log
@Component
public class StaticMigrationDDLProvider {

    private final String migrationsDirPath;

    public StaticMigrationDDLProvider(
            @Value("${application.static-migration.dir-path}") String migrationsDirPath
    ) {
        this.migrationsDirPath = migrationsDirPath;
    }

    @SneakyThrows
    public List<String> getInitQueries() {
        try (Stream<Path> paths = Files.walk(Paths.get(migrationsDirPath + "/init"))) {
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
    public String getDropAllQuery() {
        return Files.readString(Path.of(migrationsDirPath + "/drop/drop_all_static.sql"));
    }
}
