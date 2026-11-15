package io.student.rangiffler.data;

import io.qameta.allure.Step;

import java.util.ArrayList;
import java.util.List;

public final class UserWithRelationsFixture implements UserFixture {
  private final UserFixture origin;
  private final Relation relation;
  private final List<UserFixture> partners;

  public UserWithRelationsFixture(UserFixture origin, Relation relation, int count) {
    this.origin = origin;
    this.relation = relation;
    this.partners = fixtures(count);
  }

  @Override
  public TestUser user() {
    List<TestUser> users = new ArrayList<>(partners.size());
    for (UserFixture partner : partners) {
      users.add(partner.user());
    }
    return relation.attach(origin.user(), List.copyOf(users));
  }

  @Override
  public void create() {
    origin.create();
    for (UserFixture partner : partners) {
      partner.create();
      link(relation, partner);
    }
  }

  @Override
  public void close() {
    new TearDown()
        .with(() -> unlink(relation))
        .withEach(partners, UserFixture::close)
        .with(origin::close)
        .close();
  }

  @Step("Create {relation} with {partner.user.username}")
  private void link(Relation relation, UserFixture partner) {
    relation.friendship(origin.user().username(), partner.user().username()).create();
  }

  @Step("Delete {relation} of created user")
  private void unlink(Relation relation) {
    for (UserFixture partner : partners) {
      relation.friendship(origin.user().username(), partner.user().username()).delete();
    }
  }

  private static List<UserFixture> fixtures(int count) {
    if (count < 1) {
      throw new IllegalArgumentException("Expected positive relation count, got " + count);
    }
    List<UserFixture> fixtures = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      fixtures.add(new SingleUserFixture(TestUser.unique()));
    }
    return List.copyOf(fixtures);
  }
}
