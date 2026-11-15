package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.FlywaySchema;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandSchemaExtension implements SuiteExtension {
  @Override
  public void beforeAll(ExtensionContext context) {
    if (!TestConfig.schemaCheck()) {
      return;
    }
    if (!context.getTags().contains("ui")) {
      return;
    }
    SuiteExtension.super.beforeAll(context);
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    new FlywaySchema(Databases.AUTH, "rangiffler-auth").verify();
    new FlywaySchema(Databases.API, "rangiffler-api").verify();
  }
}
