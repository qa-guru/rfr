package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("UsersQueueExtension")
class UsersQueueExtensionTest {

  @Test
  @DisplayName("Четыре теста подряд на пуле из трёх зелёные")
  void shouldPassFourTestsOnPoolOfThree() {
    EngineExecutionResults results = execute(FourInARow.class);
    assertEquals(4L, results.testEvents().succeeded().count(), "Four tests on a pool of three did not succeed");
    assertEquals(3L, usernames(results).stream().distinct().count(), "Four tests did not stay within the pool of three");
  }

  @Test
  @DisplayName("После упавшего теста пользователь вернулся в очередь")
  void shouldReturnUserToQueueAfterFailedTest() {
    EngineExecutionResults failed = execute(FailingCase.class);
    assertEquals(
        "Expected failure after static user was published",
        failure(failed).getMessage(),
        "Failed test did not take a static user before failing"
    );
    EngineExecutionResults taken = execute(WholePool.class);
    List<String> names = usernames(taken);
    assertEquals(3, names.size(), "Pool of three was not taken after the failed test");
    assertTrue(names.contains(username(failed)), "Static user was not returned to the queue after the failed test");
  }

  @Test
  @DisplayName("Четвёртый @StaticUser падает по таймауту, уже взятые возвращаются")
  void shouldFailWhenPoolIsExhaustedAndReturnTakenUsers() {
    EngineExecutionResults failed = execute(Exhausted.class);
    assertEquals(
        "No static user became available within 10 seconds",
        failure(failed).getMessage(),
        "Fourth static user did not fail with the queue timeout"
    );
    EngineExecutionResults taken = execute(WholePool.class);
    assertEquals(3, usernames(taken).size(), "Taken static users were not returned after the timed out create");
  }

  private EngineExecutionResults execute(Class<?> type) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(type))
        .execute();
  }

  private List<String> usernames(EngineExecutionResults results) {
    return results.allEvents().reportingEntryPublished().stream()
        .map(item -> item.getPayload(ReportEntry.class).orElseThrow().getKeyValuePairs().get("username"))
        .toList();
  }

  private String username(EngineExecutionResults results) {
    return usernames(results).getFirst();
  }

  private Throwable failure(EngineExecutionResults results) {
    return results.testEvents().failed().stream()
        .map(item -> item.getPayload(TestExecutionResult.class).orElseThrow())
        .flatMap(result -> result.getThrowable().stream())
        .findFirst()
        .orElseThrow();
  }

  @ProbeTarget
  static final class FourInARow {
    @Test
    void first(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void second(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void third(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void fourth(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }
  }

  @ProbeTarget
  static final class FailingCase {
    @Test
    void fail(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
      throw new IllegalStateException("Expected failure after static user was published");
    }
  }

  @ProbeTarget
  static final class Exhausted {
    @Test
    void observe(
        @StaticUser TestUser first,
        @StaticUser TestUser second,
        @StaticUser TestUser third,
        @StaticUser TestUser fourth
    ) {
    }
  }

  @ProbeTarget
  static final class WholePool {
    @Test
    void first(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void second(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }

    @Test
    void third(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("username", user.username());
    }
  }
}
