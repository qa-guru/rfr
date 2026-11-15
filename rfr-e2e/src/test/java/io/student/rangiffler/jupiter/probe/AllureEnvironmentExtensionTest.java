package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;
import io.student.rangiffler.jupiter.extension.SuiteExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("AllureEnvironmentExtension")
class AllureEnvironmentExtensionTest {

  @Test
  @DisplayName("после прогона во временном каталоге результатов есть environment.properties со всеми ключами")
  void shouldWriteEnvironmentPropertiesWithAllKeys() throws IOException {
    Path directory = Files.createTempDirectory("allure-environment");
    String previousDirectory = System.getProperty("allure.results.directory");
    String previousCommit = System.getProperty("rfr.commit");
    System.setProperty("allure.results.directory", directory.toString());
    System.setProperty("rfr.commit", "abc1234");
    try {
      execute(Observed.class);
      Properties environment = this.environment(directory);
      assertEquals(
          Set.of("frontUrl", "authUrl", "apiUrl", "browser", "headless", "commit", "rangiffler-auth", "rangiffler-api"),
          environment.stringPropertyNames(),
          "environment.properties did not contain every Allure environment key"
      );
      assertEquals("abc1234", environment.getProperty("commit"), "commit was not taken from rfr.commit");
    } finally {
      this.restore("allure.results.directory", previousDirectory);
      this.restore("rfr.commit", previousCommit);
    }
  }

  @Test
  @DisplayName("класс без тега UI-тестов — environment.properties не пишется")
  void shouldSkipEnvironmentWhenClassHasNoUiTag() throws IOException {
    Path directory = Files.createTempDirectory("allure-without-ui");
    String previousDirectory = System.getProperty("allure.results.directory");
    System.setProperty("allure.results.directory", directory.toString());
    try {
      execute(Plain.class);
      assertFalse(
          Files.exists(directory.resolve("environment.properties")),
          "environment.properties was written for a class without ui"
      );
    } finally {
      this.restore("allure.results.directory", previousDirectory);
    }
  }

  @Test
  @DisplayName("afterSuite пишет файл, если beforeSuite более позднего extension упал")
  void shouldWriteEnvironmentWhenLaterBeforeSuiteFails() throws IOException {
    Path directory = Files.createTempDirectory("allure-after-failure");
    String previousDirectory = System.getProperty("allure.results.directory");
    System.setProperty("allure.results.directory", directory.toString());
    try {
      execute(AllureThenBroken.class);
      assertTrue(
          Files.exists(directory.resolve("environment.properties")),
          "environment.properties was not written after a later beforeSuite failure"
      );
    } finally {
      this.restore("allure.results.directory", previousDirectory);
    }
  }

  @Test
  @DisplayName("afterSuite не вызывается, если extension зарегистрирован после упавшего beforeAll")
  void shouldSkipEnvironmentWhenRegisteredAfterFailure() throws IOException {
    Path directory = Files.createTempDirectory("allure-after-skipped");
    String previousDirectory = System.getProperty("allure.results.directory");
    System.setProperty("allure.results.directory", directory.toString());
    try {
      execute(BrokenThenAllure.class);
      assertFalse(
          Files.exists(directory.resolve("environment.properties")),
          "environment.properties appeared though AllureEnvironmentExtension beforeAll never ran"
      );
    } finally {
      this.restore("allure.results.directory", previousDirectory);
    }
  }

  private void execute(Class<?> type) {
    EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
        .selectors(selectClass(type))
        .execute();
  }

  private Properties environment(Path directory) throws IOException {
    Properties environment = new Properties();
    try (var reader = Files.newBufferedReader(directory.resolve("environment.properties"))) {
      environment.load(reader);
    }
    return environment;
  }

  private void restore(String key, String previous) {
    if (previous == null) {
      System.clearProperty(key);
      return;
    }
    System.setProperty(key, previous);
  }

  @ProbeTarget
  @Tag("ui")
  @ExtendWith(AllureEnvironmentExtension.class)
  static final class Observed {
    @Test
    void run() {
    }
  }

  @ProbeTarget
  @ExtendWith(AllureEnvironmentExtension.class)
  static final class Plain {
    @Test
    void run() {
    }
  }

  @ProbeTarget
  @Tag("ui")
  @ExtendWith({AllureEnvironmentExtension.class, BrokenSchema.class})
  static final class AllureThenBroken {
    @Test
    void run() {
    }
  }

  @ProbeTarget
  @ExtendWith({BrokenSchema.class, AllureEnvironmentExtension.class})
  static final class BrokenThenAllure {
    @Test
    void run() {
    }
  }

  static final class BrokenSchema implements SuiteExtension {
    @Override
    public void beforeSuite(ExtensionContext context) {
      throw new IllegalStateException("Broken schema");
    }
  }
}
