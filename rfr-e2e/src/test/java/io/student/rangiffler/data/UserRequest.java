package io.student.rangiffler.data;

import io.student.rangiffler.jupiter.annotation.User;

public record UserRequest(int friends, int incomeInvitations) {
  public UserRequest {
    if (friends < 0) {
      throw new IllegalArgumentException("friends must be >= 0, got " + friends);
    }
    if (incomeInvitations < 0) {
      throw new IllegalArgumentException("incomeInvitations must be >= 0, got " + incomeInvitations);
    }
  }

  public static UserRequest from(User user) {
    return new UserRequest(user.friends(), user.incomeInvitations());
  }

  public UserFixture fixture() {
    return new SingleUserFixture(TestUser.unique());
  }
}
