package io.student.rangiffler.data.db;

import io.student.rangiffler.data.TestUser;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public final class AuthAccount implements Stored {
  private final TestUser user;

  public AuthAccount(TestUser user) {
    this.user = user;
  }

  @Override
  public void create() {
    String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.password());
    Jdbc.inTransaction(Databases.AUTH, () -> {
      int updated = Jdbc.client(Databases.AUTH)
          .sql(Jdbc.sql("sql/auth/create-user.sql"))
          .param("username", user.username())
          .param("password", encoded)
          .update();
      if (updated != 1) {
        throw new IllegalStateException("Expected one inserted row for auth user " + user.username() + ", got " + updated);
      }
      Jdbc.client(Databases.AUTH)
          .sql(Jdbc.sql("sql/auth/create-authorities.sql"))
          .param("username", user.username())
          .update();
    });
  }

  @Override
  public void delete() {
    Jdbc.inTransaction(Databases.AUTH, () -> {
      Jdbc.client(Databases.AUTH)
          .sql(Jdbc.sql("sql/auth/delete-authorities.sql"))
          .param("username", user.username())
          .update();
      Jdbc.client(Databases.AUTH)
          .sql(Jdbc.sql("sql/auth/delete-user.sql"))
          .param("username", user.username())
          .update();
    });
  }

  @Override
  public boolean exists() {
    return Jdbc.client(Databases.AUTH)
        .sql(Jdbc.sql("sql/auth/count-user.sql"))
        .param("username", user.username())
        .query((rs, rowNum) -> rs.getInt(1))
        .single() > 0;
  }
}
