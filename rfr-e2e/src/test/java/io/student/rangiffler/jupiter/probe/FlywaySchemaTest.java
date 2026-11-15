package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.db.FlywaySchema;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("jupiter")
@DisplayName("FlywaySchema")
class FlywaySchemaTest {

  @Test
  @DisplayName("изменённый файл миграции не проходит validate")
  void shouldFailWhenAppliedMigrationChecksumChanged() throws IOException {
    Path directory = Files.createTempDirectory("flyway-checksum");
    Path script = directory.resolve("V1__probe.sql");
    Files.writeString(script, "CREATE TABLE probe_one(id INT);");
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        "jdbc:h2:mem:flyway_checksum;DB_CLOSE_DELAY=-1",
        "sa",
        ""
    );
    dataSource.setDriverClassName("org.h2.Driver");
    String location = "filesystem:" + directory.toAbsolutePath().toString().replace('\\', '/');
    Flyway.configure().dataSource(dataSource).locations(location).load().migrate();
    Files.writeString(script, "CREATE TABLE probe_two(id INT);");
    IllegalStateException failure = assertThrows(
        IllegalStateException.class,
        () -> new FlywaySchema(dataSource, "PUBLIC", location).verify(),
        "Changed migration was accepted"
    );
    assertTrue(
        failure.getMessage().toLowerCase().contains("checksum"),
        "Validation error did not mention checksum: " + failure.getMessage()
    );
  }
}
