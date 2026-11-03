package io.student.rangiffler.data;

import static io.student.rangiffler.utils.RandomDataUtils.randomPassword;
import static io.student.rangiffler.utils.RandomDataUtils.randomUsername;

public final class TestUser {
  private final String username;
  private final String password;

  public TestUser(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public static TestUser unique() {
    return new TestUser(randomUsername(), randomPassword());
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }
}
