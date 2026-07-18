package com.govos.infrastructure.flyway;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationNamingTest {

    private static final Pattern VERSIONED_MIGRATION =
            Pattern.compile("^V\\d+(?:_\\d+)*__([a-z][a-z0-9_]*?)\\.sql$");

    @Test
    void shouldMatchGovosMigrationNamingConvention() throws IOException, URISyntaxException {
        List<Path> migrations = listMigrationFiles();

        assertThat(migrations).isNotEmpty();

        Set<String> versions = new HashSet<>();
        for (Path migration : migrations) {
            String fileName = migration.getFileName().toString();
            assertThat(VERSIONED_MIGRATION.matcher(fileName).matches())
                    .as("Migration file name must match V{x_y_z}__{description}.sql: %s", fileName)
                    .isTrue();
            assertThat(versions.add(extractVersion(fileName)))
                    .as("Duplicate migration version: %s", fileName)
                    .isTrue();
        }
    }

    private List<Path> listMigrationFiles() throws IOException, URISyntaxException {
        URI migrationDir = getClass().getClassLoader()
                .getResource("db/migration")
                .toURI();
        try (Stream<Path> paths = Files.list(Path.of(migrationDir))) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .toList();
        }
    }

    private String extractVersion(String fileName) {
        int separator = fileName.indexOf("__");
        return separator > 0 ? fileName.substring(0, separator) : fileName;
    }
}
