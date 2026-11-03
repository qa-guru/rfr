package io.student.rangiffler.data.db;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class Jdbc {
  private Jdbc() {
  }

  public static JdbcClient client(Databases database) {
    return JdbcClient.create(database.dataSource());
  }

  public static void inTransaction(Databases database, Runnable action) {
    new TransactionTemplate(database.transactionManager())
        .executeWithoutResult(status -> action.run());
  }

  public static String sql(String resource) {
    ClassPathResource classpath = new ClassPathResource(resource);
    try (InputStream stream = classpath.getInputStream()) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot load SQL resource " + resource, exception);
    }
  }
}
