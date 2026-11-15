package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.FlywaySchema;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AllureEnvironmentExtension implements SuiteExtension {
  @Override
  public void beforeAll(ExtensionContext context) {
    if (!context.getTags().contains("ui")) {
      return;
    }
    SuiteExtension.super.beforeAll(context);
  }

  @Override
  public void afterSuite(ExtensionContext context) {
    Path directory = Path.of(System.getProperty("allure.results.directory", "build/allure-results"));
    Map<String, String> environment = new LinkedHashMap<>();
    environment.put("frontUrl", TestConfig.frontUrl());
    environment.put("authUrl", TestConfig.authUrl());
    environment.put("apiUrl", TestConfig.apiUrl());
    environment.put("browser", TestConfig.browser());
    environment.put("headless", Boolean.toString(TestConfig.headless()));
    environment.put("commit", TestConfig.commit());
    environment.put("rangiffler-auth", new FlywaySchema(Databases.AUTH, "rangiffler-auth").version());
    environment.put("rangiffler-api", new FlywaySchema(Databases.API, "rangiffler-api").version());
    StringBuilder text = new StringBuilder();
    for (Map.Entry<String, String> entry : environment.entrySet()) {
      text.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
    }
    try {
      Files.createDirectories(directory);
      Files.writeString(directory.resolve("environment.properties"), text, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot write Allure environment.properties in " + directory, exception);
    }
  }
}
