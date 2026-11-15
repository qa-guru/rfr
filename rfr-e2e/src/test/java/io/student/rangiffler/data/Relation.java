package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;

import java.util.List;

public enum Relation {
  FRIEND {
    @Override
    Friendship friendship(String user, String partner) {
      return new Friendship(user, partner, FriendshipStatus.ACCEPTED);
    }

    @Override
    TestUser attach(TestUser user, List<TestUser> partners) {
      return user.withFriends(partners);
    }
  },
  INCOME_INVITATION {
    @Override
    Friendship friendship(String user, String partner) {
      return new Friendship(partner, user, FriendshipStatus.PENDING);
    }

    @Override
    TestUser attach(TestUser user, List<TestUser> partners) {
      return user.withIncomeInvitations(partners);
    }
  },
  OUTCOME_INVITATION {
    @Override
    Friendship friendship(String user, String partner) {
      return new Friendship(user, partner, FriendshipStatus.PENDING);
    }

    @Override
    TestUser attach(TestUser user, List<TestUser> partners) {
      return user.withOutcomeInvitations(partners);
    }
  };

  abstract Friendship friendship(String user, String partner);

  abstract TestUser attach(TestUser user, List<TestUser> partners);
}
