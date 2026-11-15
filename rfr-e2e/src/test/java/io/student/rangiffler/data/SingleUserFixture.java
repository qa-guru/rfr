package io.student.rangiffler.data;

import io.qameta.allure.Step;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Invitations;
import io.student.rangiffler.data.db.Profile;

public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private final AuthAccount account;
  private final Profile profile;

  public SingleUserFixture(TestUser user) {
    this.user = user;
    this.account = new AuthAccount(user);
    this.profile = new Profile(user.username());
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void create() {
    createUser();
    createProfile();
  }

  @Step("Create user in rangiffler-auth")
  private void createUser() {
    account.create();
  }

  @Step("Create profile in rangiffler-api")
  private void createProfile() {
    profile.create();
  }

  @Step("Delete created user from databases")
  @Override
  public void close() {
    new TearDown()
        .with(new Friendships(user.username())::delete)
        .with(new Invitations(user.username())::delete)
        .with(profile::delete)
        .with(account::delete)
        .close();
  }
}
