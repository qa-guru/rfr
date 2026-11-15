package io.student.rangiffler.data.db;

public final class Friendships {
  private final String username;

  public Friendships(String username) {
    this.username = username;
  }

  public void delete() {
    Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/delete-friends.sql"))
        .param("username", username)
        .update();
  }

  public boolean exists() {
    return count(FriendshipStatus.ACCEPTED) > 0;
  }

  public int accepted() {
    return count(FriendshipStatus.ACCEPTED);
  }

  public int pending() {
    return count(FriendshipStatus.PENDING);
  }

  public int remaining(String id) {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/count-friendships-by-id.sql"))
        .param("id", id)
        .query((rs, rowNum) -> rs.getInt(1))
        .single();
  }

  private int count(FriendshipStatus status) {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/count-friendships.sql"))
        .param("username", username)
        .param("status", status.name())
        .query((rs, rowNum) -> rs.getInt(1))
        .single();
  }
}
