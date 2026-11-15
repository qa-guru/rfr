package io.student.rangiffler.data.db;

public final class Invitations {
  private final String username;

  public Invitations(String username) {
    this.username = username;
  }

  public void delete() {
    Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/delete-invitations.sql"))
        .param("username", username)
        .update();
  }

  public boolean exists() {
    return count() > 0;
  }

  public int count() {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/count-friendships.sql"))
        .param("username", username)
        .param("status", FriendshipStatus.PENDING.name())
        .query((rs, rowNum) -> rs.getInt(1))
        .single();
  }
}
