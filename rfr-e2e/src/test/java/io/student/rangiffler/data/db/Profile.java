package io.student.rangiffler.data.db;

public final class Profile implements Stored {
  private final String username;

  public Profile(String username) {
    this.username = username;
  }

  @Override
  public void create() {
    int updated = Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/create-profile.sql"))
        .param("username", username)
        .update();
    if (updated != 1) {
      throw new IllegalStateException("Expected one inserted row for api profile " + username + ", got " + updated);
    }
  }

  @Override
  public void delete() {
    Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/delete-profile.sql"))
        .param("username", username)
        .update();
  }

  @Override
  public boolean exists() {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/count-profile.sql"))
        .param("username", username)
        .query((rs, rowNum) -> rs.getInt(1))
        .single() > 0;
  }

  public String id() {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/select-profile-id.sql"))
        .param("username", username)
        .query(String.class)
        .single();
  }
}
