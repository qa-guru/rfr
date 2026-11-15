package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.extension.SuiteExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("SuiteExtension")
class SuiteExtensionTest {

  @BeforeEach
  void reset() {
    Tracked.befores.set(0);
    Tracked.afters.set(0);
    Tracked.events.clear();
    Tracked.beforeFailure = null;
    Tracked.afterFailure = null;
    Tracked.beforeContext = null;
    Tracked.afterContext = null;
  }

  @Test
  @DisplayName("beforeSuite вызывается один раз на весь прогон из двух классов")
  void shouldCallBeforeSuiteOnceForTwoClasses() {
    execute(First.class, Second.class);
    assertEquals(1, Tracked.befores.get(), "beforeSuite ran more than once for a single test run");
  }

  @Test
  @DisplayName("afterSuite вызывается один раз на весь прогон из двух классов")
  void shouldCallAfterSuiteOnceForTwoClasses() {
    execute(First.class, Second.class);
    assertEquals(1, Tracked.afters.get(), "afterSuite ran more than once for a single test run");
  }

  @Test
  @DisplayName("afterSuite вызывается после тестов")
  void shouldCallAfterSuiteAfterTests() {
    execute(First.class, Second.class);
    assertEquals(
        List.of("before", "test", "test", "after"),
        Tracked.events,
        "Suite callbacks did not wrap the whole test run"
    );
  }

  @Test
  @DisplayName("Упавший тест не отменяет afterSuite")
  void shouldCallAfterSuiteWhenTestFails() {
    execute(FailingCase.class);
    assertEquals(1, Tracked.befores.get(), "beforeSuite did not run once for a failed test");
    assertEquals(1, Tracked.afters.get(), "afterSuite did not run after a failed test");
  }

  @Test
  @DisplayName("Исключение в beforeSuite не повторяет beforeSuite на следующем классе")
  void shouldCallBeforeSuiteOnceWhenBeforeSuiteFails() {
    Tracked.beforeFailure = new IllegalStateException("Expected beforeSuite failure");
    execute(First.class, Second.class);
    assertEquals(1, Tracked.befores.get(), "Failed beforeSuite was retried in the same test run");
  }

  @Test
  @DisplayName("Исключение в beforeSuite не отменяет afterSuite")
  void shouldCallAfterSuiteWhenBeforeSuiteFails() {
    Tracked.beforeFailure = new IllegalStateException("Expected beforeSuite failure");
    execute(First.class, Second.class);
    assertEquals(1, Tracked.afters.get(), "afterSuite did not run after a failed beforeSuite");
  }

  @Test
  @DisplayName("Упавший beforeSuite не пускает тесты")
  void shouldNotStartTestsWhenBeforeSuiteFails() {
    Tracked.beforeFailure = new IllegalStateException("Expected beforeSuite failure");
    assertEquals(
        0L,
        execute(First.class, Second.class).testEvents().started().count(),
        "Failed beforeSuite started tests"
    );
  }

  @Test
  @DisplayName("Исключение в afterSuite оставляет один вызов afterSuite")
  void shouldCallAfterSuiteOnceWhenAfterSuiteFails() {
    Tracked.afterFailure = new IllegalStateException("Expected afterSuite failure");
    execute(First.class, Second.class);
    assertEquals(1, Tracked.afters.get(), "Failed afterSuite ran more than once");
  }

  @Test
  @DisplayName("Упавший afterSuite не завалит прошедшие тесты")
  void shouldKeepPassedTestsWhenAfterSuiteFails() {
    Tracked.afterFailure = new IllegalStateException("Expected afterSuite failure");
    assertEquals(
        2L,
        execute(First.class, Second.class).testEvents().succeeded().count(),
        "Failed afterSuite failed already passed tests"
    );
  }

  @Test
  @DisplayName("afterSuite получает тот же корневой контекст, что и beforeSuite")
  void shouldPassRootContextToAfterSuite() {
    execute(First.class);
    assertEquals(
        Tracked.beforeContext.getRoot(),
        Tracked.afterContext,
        "afterSuite did not receive the root context from beforeSuite"
    );
  }

  @Test
  @DisplayName("Вложенные классы не умножают beforeSuite и afterSuite")
  void shouldCallCallbacksOnceForNestedClasses() {
    execute(NestedRun.class);
    assertEquals(1, Tracked.befores.get(), "beforeSuite ran more than once for nested classes");
    assertEquals(1, Tracked.afters.get(), "afterSuite ran more than once for nested classes");
  }

  private EngineExecutionResults execute(Class<?> type) {
    return this.execute(selectClass(type));
  }

  private EngineExecutionResults execute(Class<?> first, Class<?> second) {
    return this.execute(selectClass(first), selectClass(second));
  }

  private EngineExecutionResults execute(DiscoverySelector... selectors) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectors)
        .execute();
  }

  static final class Tracked implements SuiteExtension {
    static final AtomicInteger befores = new AtomicInteger();
    static final AtomicInteger afters = new AtomicInteger();
    static final List<String> events = new CopyOnWriteArrayList<>();
    static RuntimeException beforeFailure;
    static RuntimeException afterFailure;
    static ExtensionContext beforeContext;
    static ExtensionContext afterContext;

    @Override
    public void beforeSuite(ExtensionContext context) {
      beforeContext = context;
      befores.incrementAndGet();
      events.add("before");
      if (beforeFailure != null) {
        throw beforeFailure;
      }
    }

    @Override
    public void afterSuite(ExtensionContext context) {
      afterContext = context;
      afters.incrementAndGet();
      events.add("after");
      if (afterFailure != null) {
        throw afterFailure;
      }
    }
  }

  @ProbeTarget
  @ExtendWith(Tracked.class)
  static final class First {
    @Test
    void run() {
      Tracked.events.add("test");
    }
  }

  @ProbeTarget
  @ExtendWith(Tracked.class)
  static final class Second {
    @Test
    void run() {
      Tracked.events.add("test");
    }
  }

  @ProbeTarget
  @ExtendWith(Tracked.class)
  static final class FailingCase {
    @Test
    void fail() {
      Tracked.events.add("test");
      throw new IllegalStateException("Expected failure after beforeSuite");
    }
  }

  @ProbeTarget
  @ExtendWith(Tracked.class)
  static final class NestedRun {
    @Nested
    final class Inner {
      @Test
      void run() {
        Tracked.events.add("test");
      }
    }

    @Nested
    final class Other {
      @Test
      void run() {
        Tracked.events.add("test");
      }
    }
  }
}
