package io.student.rangiffler.data;

import io.student.rangiffler.jupiter.annotation.User;

public final class UserRequest {
  private final int friends;
  private final int incomeInvitations;
  private final int outcomeInvitations;

  public UserRequest(User user) {
    this(user.friends(), user.incomeInvitations(), user.outcomeInvitations());
  }

  public UserRequest(int friends, int incomeInvitations, int outcomeInvitations) {
    this.friends = nonNegative(friends, "friends");
    this.incomeInvitations = nonNegative(incomeInvitations, "incomeInvitations");
    this.outcomeInvitations = nonNegative(outcomeInvitations, "outcomeInvitations");
  }

  public int friends() {
    return friends;
  }

  public int incomeInvitations() {
    return incomeInvitations;
  }

  public int outcomeInvitations() {
    return outcomeInvitations;
  }

  public UserFixture fixture() {
    UserFixture origin = new SingleUserFixture(TestUser.unique());
    if (friends > 0) {
      origin = new UserWithRelationsFixture(origin, Relation.FRIEND, friends);
    }
    if (incomeInvitations > 0) {
      origin = new UserWithRelationsFixture(origin, Relation.INCOME_INVITATION, incomeInvitations);
    }
    if (outcomeInvitations > 0) {
      origin = new UserWithRelationsFixture(origin, Relation.OUTCOME_INVITATION, outcomeInvitations);
    }
    return origin;
  }

  private static int nonNegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException("Expected non-negative " + name + ", got " + value);
    }
    return value;
  }
}
