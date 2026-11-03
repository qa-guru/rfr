package io.student.rangiffler.data;

import io.qameta.allure.Step;
import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.ArrayList;
import java.util.List;

public final class UserFixture implements AutoCloseable {
  private final TestUser user;
  private final JdbcClient auth;
  private final JdbcClient api;

  public UserFixture(TestUser user) {
    this.user = user;
    this.auth = Jdbc.client(Databases.AUTH);
    this.api = Jdbc.client(Databases.API);
  }

  public TestUser user() {
    return user;
  }

  @Step("Create user in rangiffler-auth")
  public void create() {
    String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.password());
    Jdbc.inTransaction(Databases.AUTH, () -> {
      requireOneRow(auth.sql(Jdbc.sql("sql/auth/create-user.sql"))
          .param("username", user.username())
          .param("password", encoded)
          .update(), "auth user " + user.username());
      auth.sql(Jdbc.sql("sql/auth/create-authorities.sql"))
          .param("username", user.username())
          .update();
    });
  }

  @Step("Delete created user from databases")
  @Override
  public void close() {
    List<Runnable> steps = List.of(this::deleteProfile, this::deleteUser);
    List<RuntimeException> failures = new ArrayList<>();
    for (Runnable step : steps) {
      try {
        step.run();
      } catch (RuntimeException failure) {
        failures.add(failure);
      }
    }
    if (failures.isEmpty()) {
      return;
    }
    RuntimeException first = failures.getFirst();
    failures.stream().skip(1).forEach(first::addSuppressed);
    throw first;
  }

  private void deleteProfile() {
    api.sql(Jdbc.sql("sql/api/delete-profile.sql"))
        .param("username", user.username())
        .update();
  }

  private void deleteUser() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      auth.sql(Jdbc.sql("sql/auth/delete-authorities.sql"))
          .param("username", user.username())
          .update();
      auth.sql(Jdbc.sql("sql/auth/delete-user.sql"))
          .param("username", user.username())
          .update();
    });
  }

  private static void requireOneRow(int updated, String what) {
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for " + what + ", got " + updated);
    }
  }
}
