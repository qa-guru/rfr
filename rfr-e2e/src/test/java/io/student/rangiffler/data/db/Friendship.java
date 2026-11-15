package io.student.rangiffler.data.db;

public final class Friendship implements Stored {
  private final String requester;
  private final String addressee;
  private final FriendshipStatus status;

  public Friendship(String requester, String addressee) {
    this(requester, addressee, FriendshipStatus.ACCEPTED);
  }

  public Friendship(String requester, String addressee, FriendshipStatus status) {
    this.requester = requester;
    this.addressee = addressee;
    this.status = status;
  }

  @Override
  public void create() {
    int updated = Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/create-friendship.sql"))
        .param("requester", requester)
        .param("addressee", addressee)
        .param("status", status.name())
        .update();
    if (updated != 1) {
      throw new IllegalStateException(
          "Expected one inserted friendship for " + requester + " and " + addressee + ", got " + updated
      );
    }
  }

  @Override
  public void delete() {
    Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/delete-friendship.sql"))
        .param("requester", requester)
        .param("addressee", addressee)
        .param("status", status.name())
        .update();
  }

  @Override
  public boolean exists() {
    return Jdbc.client(Databases.API)
        .sql(Jdbc.sql("sql/api/exists-friendship.sql"))
        .param("requester", requester)
        .param("addressee", addressee)
        .param("status", status.name())
        .query((rs, rowNum) -> rs.getInt(1))
        .single() > 0;
  }
}
