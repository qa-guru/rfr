package io.student.rangiffler.data.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.ValidateResult;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public final class FlywaySchema implements Schema {
  private final DataSource dataSource;
  private final String schema;
  private final String location;

  public FlywaySchema(Databases database, String schema) {
    this(database.dataSource(), schema, "classpath:db/migration/" + schema);
  }

  public FlywaySchema(DataSource dataSource, String schema, String location) {
    this.dataSource = dataSource;
    this.schema = schema;
    this.location = location;
  }

  @Override
  public void verify() {
    if (this.location.startsWith("classpath:") && this.scripts().isEmpty()) {
      throw new IllegalStateException(
          "Backend Flyway scripts for " + this.schema
              + " were not found on the classpath; run e2e through Gradle so migrations are copied from the backend"
      );
    }
    ValidateResult result = this.flyway().validateWithResult();
    if (result.validationSuccessful) {
      return;
    }
    throw new IllegalStateException(
        "Flyway validation failed for " + this.schema + ": "
            + String.join("; ", result.getAllErrorMessages())
            + "; restart the backend for " + this.schema
            + " so Flyway applies pending migrations or recreate MySQL volume mysql-volume if checksums differ"
    );
  }

  @Override
  public String version() {
    try {
      MigrationInfo current = this.flyway().info().current();
      if (current == null || current.getVersion() == null) {
        return "0";
      }
      return current.getVersion().toString();
    } catch (FlywayException exception) {
      return "unavailable";
    }
  }

  private Flyway flyway() {
    return Flyway.configure()
        .dataSource(this.dataSource)
        .locations(this.location)
        .load();
  }

  private List<String> scripts() {
    Resource[] resources;
    try {
      resources = new PathMatchingResourcePatternResolver()
          .getResources("classpath:db/migration/" + this.schema + "/V*__*.sql");
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot list Flyway scripts for " + this.schema, exception);
    }
    List<String> versions = new ArrayList<>();
    for (Resource resource : resources) {
      String name = resource.getFilename();
      if (name == null) {
        continue;
      }
      int separator = name.indexOf("__");
      if (!name.startsWith("V") || separator < 2) {
        continue;
      }
      versions.add(name.substring(1, separator));
    }
    return versions;
  }
}
