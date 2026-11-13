package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.data.db.Jdbc;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private final JdbcClient auth = Jdbc.client(Databases.AUTH);
  private final JdbcClient api = Jdbc.client(Databases.API);

  public SingleUserFixture(TestUser user) {
    this.user = user;
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void create() {
    String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.password());
    Jdbc.inTransaction(Databases.AUTH, () -> {
      requireOneRow(auth.sql(Jdbc.sql("auth/create-user.sql"))
          .param("username", user.username())
          .param("password", encoded)
          .update(), "auth user " + user.username());
      auth.sql(Jdbc.sql("auth/create-authorities.sql"))
          .param("username", user.username())
          .update();
    });
    requireOneRow(api.sql(Jdbc.sql("api/create-profile.sql"))
        .param("username", user.username())
        .update(), "Api user profile " + user.username());
  }

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
    if (!failures.isEmpty()) {
      RuntimeException first = failures.getFirst();
      failures.stream().skip(1).forEach(first::addSuppressed);
      throw first;
    }
  }

  private void deleteProfile() {
    api.sql(Jdbc.sql("api/delete-profile.sql")).param("username", user.username()).update();
  }

  private void deleteUser() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      auth.sql(Jdbc.sql("auth/delete-authorities.sql")).param("username", user.username()).update();
      auth.sql(Jdbc.sql("auth/delete-user.sql")).param("username", user.username()).update();
    });
  }

  private static void requireOneRow(int updated, String what) {
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for " + what + ", got " + updated);
    }
  }
}
