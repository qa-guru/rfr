package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class StandReadinessExtension implements SuiteExtension {
  private static final Duration REQUEST = Duration.ofSeconds(2);

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!TestConfig.standCheck()) {
      return;
    }
    if (!context.getTags().contains("ui")) {
      return;
    }
    SuiteExtension.super.beforeAll(context);
  }

  @Override
  public void beforeSuite(ExtensionContext context) {
    new Stand(Duration.ofSeconds(TestConfig.standTimeout())).await();
  }

  private static final class Stand {
    private final Duration limit;

    private Stand(Duration limit) {
      this.limit = limit;
    }

    private void await() {
      long deadline = System.nanoTime() + this.limit.toNanos();
      try (HttpClient client = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .connectTimeout(REQUEST)
          .build()) {
        while (true) {
          List<String> down = this.down(client);
          if (down.isEmpty()) {
            return;
          }
          long left = deadline - System.nanoTime();
          if (left <= 0) {
            throw new IllegalStateException(
                "Stand was not ready in " + this.limit.toSeconds() + "s: " + String.join(", ", down)
            );
          }
          TimeUnit.NANOSECONDS.sleep(Math.min(TimeUnit.MILLISECONDS.toNanos(500), left));
        }
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Stand wait was interrupted", exception);
      }
    }

    private List<String> down(HttpClient client) {
      List<String> down = new ArrayList<>();
      if (!this.mysql()) {
        down.add("mysql; start it with docker compose up -d --wait");
      }
      if (!this.http(client, this.uri(TestConfig.authUrl(), "/.well-known/openid-configuration"), "GET", null)) {
        down.add("rfr-auth; start it with ./gradlew runAll");
      }
      if (!this.http(client, this.uri(TestConfig.apiUrl(), "/graphql"), "POST", "{\"query\":\"{__typename}\"}")) {
        down.add("rfr-api; start it with ./gradlew runAll");
      }
      if (!this.http(client, this.uri(TestConfig.frontUrl(), "/"), "GET", null)) {
        down.add("rfr-gql-client; start it with npm run dev");
      }
      return down;
    }

    private boolean mysql() {
      String raw = TestConfig.dbUrl("rangiffler-auth");
      int scheme = raw.indexOf("://");
      String rest = scheme < 0 ? raw : raw.substring(scheme + 3);
      int slash = rest.indexOf('/');
      String hostPort = slash < 0 ? rest : rest.substring(0, slash);
      int colon = hostPort.lastIndexOf(':');
      String host = colon < 0 ? hostPort : hostPort.substring(0, colon);
      int port = colon < 0 ? 3306 : Integer.parseInt(hostPort.substring(colon + 1));
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(host, port), (int) REQUEST.toMillis());
        return true;
      } catch (IOException exception) {
        return false;
      }
    }

    private URI uri(String origin, String path) {
      return URI.create(origin + "/").resolve(path.startsWith("/") ? path.substring(1) : path);
    }

    private boolean http(HttpClient client, URI origin, String method, String body) {
      for (URI target : this.targets(origin)) {
        if (this.send(client, target, method, body)) {
          return true;
        }
      }
      return false;
    }

    private Set<URI> targets(URI origin) {
      Set<URI> targets = new LinkedHashSet<>();
      targets.add(origin);
      String host = origin.getHost();
      if (host == null) {
        return targets;
      }
      try {
        for (InetAddress address : InetAddress.getAllByName(host)) {
          targets.add(this.rewrite(origin, address));
        }
      } catch (UnknownHostException exception) {
        return targets;
      }
      return targets;
    }

    private URI rewrite(URI origin, InetAddress address) {
      String host = address.getHostAddress();
      int zone = host.indexOf('%');
      if (zone >= 0) {
        host = host.substring(0, zone);
      }
      if (host.indexOf(':') >= 0) {
        host = "[" + host + "]";
      }
      int port = origin.getPort();
      String path = origin.getRawPath();
      if (path == null || path.isEmpty()) {
        path = "/";
      }
      String query = origin.getRawQuery() == null ? "" : "?" + origin.getRawQuery();
      return URI.create(origin.getScheme() + "://" + host + (port < 0 ? "" : ":" + port) + path + query);
    }

    private boolean send(HttpClient client, URI target, String method, String body) {
      try {
        HttpRequest.Builder request = HttpRequest.newBuilder(target).timeout(REQUEST);
        if (body == null) {
          request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
          request.header("Content-Type", "application/json")
              .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        int status = client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
        return status >= 200 && status < 300;
      } catch (IOException | InterruptedException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        return false;
      }
    }
  }
}
