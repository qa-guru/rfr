package io.student.rangiffler.jupiter.probe;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.student.rangiffler.jupiter.extension.StandReadinessExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("StandReadinessExtension")
class StandReadinessExtensionTest {

  @Test
  @DisplayName("сервис начал отвечать через 2 с — тесты выполняются")
  void shouldRunTestsWhenServiceStartsAfterTwoSeconds() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    HttpServer http = http(hits);
    try (ServerSocket mysql = mysql()) {
      Thread starter = new Thread(() -> {
        try {
          Thread.sleep(2000);
          http.start();
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
        }
      });
      starter.start();
      Properties previous = pointTo(http, mysql, "10", null);
      try {
        assertEquals(
            1L,
            execute(WaitingUi.class).testEvents().succeeded().count(),
            "Tests did not run after the service started"
        );
        assertTrue(hits.get() > 0, "Ready HTTP server was never queried");
      } finally {
        restore(previous);
        starter.interrupt();
      }
    } finally {
      http.stop(0);
    }
  }

  @Test
  @DisplayName("сервис не ответил за таймаут — тесты не выполняются, а в ошибке есть имя сервиса")
  void shouldSkipTestsWhenServiceMissesTimeout() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    HttpServer http = http(hits);
    try (ServerSocket mysql = mysql()) {
      Properties previous = pointTo(http, mysql, "2", null);
      try {
        EngineExecutionResults results = execute(WaitingUi.class);
        assertEquals(0L, results.testEvents().started().count(), "Tests ran while the stand was down");
        String message = failure(results);
        assertTrue(message.contains("rfr-gql-client"), "Timeout error did not name rfr-gql-client: " + message);
      } finally {
        restore(previous);
      }
    } finally {
      http.stop(0);
    }
  }

  @Test
  @DisplayName("OIDC и GraphQL отвечают 503 — тесты не выполняются")
  void shouldSkipTestsWhenReadyEndpointsReturnErrors() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    HttpServer http = this.http(hits, 200, 503, 503);
    http.start();
    try (ServerSocket mysql = mysql()) {
      Properties previous = pointTo(http, mysql, "2", null);
      try {
        EngineExecutionResults results = execute(WaitingUi.class);
        assertEquals(0L, results.testEvents().started().count(), "Tests ran while OIDC and GraphQL returned errors");
        String message = failure(results);
        assertTrue(
            message.contains("rfr-auth") || message.contains("rfr-api"),
            "Timeout error did not name rfr-auth or rfr-api: " + message
        );
      } finally {
        restore(previous);
      }
    } finally {
      http.stop(0);
    }
  }

  @Test
  @DisplayName("класс без тега UI-тестов — к сервису нет ни одного запроса")
  void shouldSkipStandWhenClassHasNoUiTag() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    HttpServer http = http(hits);
    http.start();
    try (ServerSocket mysql = mysql()) {
      Properties previous = pointTo(http, mysql, "10", null);
      try {
        assertEquals(1L, execute(Plain.class).testEvents().succeeded().count(), "Class without ui did not run");
        assertEquals(0, hits.get(), "Stand was queried for a class without ui");
      } finally {
        restore(previous);
      }
    } finally {
      http.stop(0);
    }
  }

  @Test
  @DisplayName("проверка выключена — к сервису нет ни одного запроса")
  void shouldSkipStandWhenCheckDisabled() throws Exception {
    AtomicInteger hits = new AtomicInteger();
    HttpServer http = http(hits);
    http.start();
    try (ServerSocket mysql = mysql()) {
      Properties previous = pointTo(http, mysql, "10", "false");
      try {
        assertEquals(1L, execute(WaitingUi.class).testEvents().succeeded().count(), "Disabled stand check did not run tests");
        assertEquals(0, hits.get(), "Stand was queried while rfr.standCheck=false");
      } finally {
        restore(previous);
      }
    } finally {
      http.stop(0);
    }
  }

  private EngineExecutionResults execute(Class<?> type) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .configurationParameter("junit.jupiter.extensions.autodetection.enabled", "false")
        .selectors(selectClass(type))
        .execute();
  }

  private String failure(EngineExecutionResults results) {
    return results.allEvents().failed().stream()
        .map(event -> event.getPayload(TestExecutionResult.class).orElseThrow())
        .flatMap(result -> result.getThrowable().stream())
        .map(Throwable::getMessage)
        .findFirst()
        .orElseThrow();
  }

  private HttpServer http(AtomicInteger hits) throws IOException {
    return this.http(hits, 200, 200, 200);
  }

  private HttpServer http(AtomicInteger hits, int root, int oidc, int graphql) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/", exchange -> this.reply(hits, exchange, root));
    server.createContext("/.well-known/openid-configuration", exchange -> this.reply(hits, exchange, oidc));
    server.createContext("/graphql", exchange -> this.reply(hits, exchange, graphql));
    return server;
  }

  private void reply(AtomicInteger hits, HttpExchange exchange, int status) throws IOException {
    hits.incrementAndGet();
    exchange.getRequestBody().readAllBytes();
    exchange.sendResponseHeaders(status, 0);
    exchange.close();
  }

  private ServerSocket mysql() throws IOException {
    return new ServerSocket();
  }

  private Properties pointTo(HttpServer http, ServerSocket mysql, String timeout, String check) throws IOException {
    if (!mysql.isBound()) {
      mysql.bind(new InetSocketAddress("127.0.0.1", 0));
    }
    String base = "http://127.0.0.1:" + http.getAddress().getPort();
    Properties previous = new Properties();
    this.store(previous, "rfr.authUrl", base);
    this.store(previous, "rfr.apiUrl", base);
    this.store(previous, "rfr.frontUrl", base);
    this.store(previous, "rfr.dbUrl", "jdbc:mysql://127.0.0.1:" + mysql.getLocalPort());
    this.store(previous, "rfr.standTimeout", timeout);
    this.store(previous, "rfr.standCheck", check);
    return previous;
  }

  private void store(Properties previous, String key, String value) {
    String current = System.getProperty(key);
    if (current != null) {
      previous.setProperty(key, current);
    }
    if (value == null) {
      System.clearProperty(key);
      return;
    }
    System.setProperty(key, value);
  }

  private void restore(Properties previous) {
    for (String key : new String[] {
        "rfr.authUrl", "rfr.apiUrl", "rfr.frontUrl", "rfr.dbUrl", "rfr.standTimeout", "rfr.standCheck"
    }) {
      if (previous.containsKey(key)) {
        System.setProperty(key, previous.getProperty(key));
      } else {
        System.clearProperty(key);
      }
    }
  }

  @ProbeTarget
  @Tag("ui")
  @ExtendWith(StandReadinessExtension.class)
  static final class WaitingUi {
    @Test
    void run() {
    }
  }

  @ProbeTarget
  @ExtendWith(StandReadinessExtension.class)
  static final class Plain {
    @Test
    void run() {
    }
  }
}
