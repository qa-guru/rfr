package io.student.rangiffler.data;

import java.util.List;

import static io.student.rangiffler.utils.RandomDataUtils.randomPassword;
import static io.student.rangiffler.utils.RandomDataUtils.randomUsername;

public final class TestUser {
  private final String username;
  private final String password;
  private final List<TestUser> friends;
  private final List<TestUser> incomeInvitations;
  private final List<TestUser> outcomeInvitations;

  public TestUser(String username, String password) {
    this(username, password, List.of(), List.of(), List.of());
  }

  public TestUser(
      String username,
      String password,
      List<TestUser> friends,
      List<TestUser> incomeInvitations,
      List<TestUser> outcomeInvitations
  ) {
    this.username = username;
    this.password = password;
    this.friends = List.copyOf(friends);
    this.incomeInvitations = List.copyOf(incomeInvitations);
    this.outcomeInvitations = List.copyOf(outcomeInvitations);
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

  public List<TestUser> friends() {
    return friends;
  }

  public List<TestUser> incomeInvitations() {
    return incomeInvitations;
  }

  public List<TestUser> outcomeInvitations() {
    return outcomeInvitations;
  }

  public TestUser withFriends(List<TestUser> friends) {
    return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
  }

  public TestUser withIncomeInvitations(List<TestUser> incomeInvitations) {
    return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
  }

  public TestUser withOutcomeInvitations(List<TestUser> outcomeInvitations) {
    return new TestUser(username, password, friends, incomeInvitations, outcomeInvitations);
  }
}
