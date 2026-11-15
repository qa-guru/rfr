package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("StandSchemaExtension")
class StandSchemaExtensionTest {

  @Test
  @DisplayName("класс без тега UI-тестов — сверка схем не выполняется")
  void shouldSkipSchemaWhenClassHasNoUiTag() {
    Properties previous = this.store("rfr.schemaCheck", null);
    try {
      assertEquals(1L, this.execute(Plain.class).testEvents().succeeded().count(), "Class without ui did not run");
    } finally {
      this.restore(previous);
    }
  }

  @Test
  @DisplayName("проверка схем выключена — тесты выполняются")
  void shouldSkipSchemaWhenCheckDisabled() {
    Properties previous = this.store("rfr.schemaCheck", "false");
    try {
      assertEquals(
          1L,
          this.execute(WaitingUi.class).testEvents().succeeded().count(),
          "Disabled schema check did not run tests"
      );
    } finally {
      this.restore(previous);
    }
  }

  private EngineExecutionResults execute(Class<?> type) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
        .selectors(selectClass(type))
        .execute();
  }

  private Properties store(String key, String value) {
    Properties previous = new Properties();
    String current = System.getProperty(key);
    if (current != null) {
      previous.setProperty(key, current);
    }
    if (value == null) {
      System.clearProperty(key);
      return previous;
    }
    System.setProperty(key, value);
    return previous;
  }

  private void restore(Properties previous) {
    if (previous.containsKey("rfr.schemaCheck")) {
      System.setProperty("rfr.schemaCheck", previous.getProperty("rfr.schemaCheck"));
      return;
    }
    System.clearProperty("rfr.schemaCheck");
  }

  @ProbeTarget
  @Tag("ui")
  @ExtendWith(StandSchemaExtension.class)
  static final class WaitingUi {
    @Test
    void run() {
    }
  }

  @ProbeTarget
  @ExtendWith(StandSchemaExtension.class)
  static final class Plain {
    @Test
    void run() {
    }
  }
}
