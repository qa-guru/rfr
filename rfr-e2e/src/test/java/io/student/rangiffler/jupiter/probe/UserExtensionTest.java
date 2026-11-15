package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("jupiter")
@DisplayName("UserExtension")
class UserExtensionTest {

  @Test
  @DisplayName("Тест с @User проходит")
  void shouldSucceedWhenUserIsAnnotated() {
    assertEquals(1L, execute(Observed.class).testEvents().succeeded().count(), "Test with @User did not succeed");
  }

  @Test
  @DisplayName("Во время теста пользователь есть в auth")
  void shouldKeepAccountDuringTest() {
    assertEquals("true", published(Observed.class).get("account"), "Account did not exist during the test");
  }

  @Test
  @DisplayName("Во время теста у пользователя есть профиль в api")
  void shouldKeepProfileDuringTest() {
    assertEquals("true", published(Observed.class).get("profile"), "Profile did not exist during the test");
  }

  @Test
  @DisplayName("Во время теста друг связан со статусом ACCEPTED")
  void shouldKeepAcceptedFriendshipDuringTest() {
    assertEquals("true", published(Observed.class).get("friendship"), "Accepted friendship did not exist during the test");
  }

  @Test
  @DisplayName("Во время теста входящая заявка связана со статусом PENDING")
  void shouldKeepPendingInvitationDuringTest() {
    assertEquals("true", published(Observed.class).get("invitation"), "Pending invitation did not exist during the test");
  }

  @Test
  @DisplayName("В параметр приходит пользователь с запрошенными друзьями")
  void shouldResolveRequestedFriends() {
    assertEquals("1", published(Observed.class).get("friends"), "Resolved user did not carry the requested friends");
  }

  @Test
  @DisplayName("В параметр приходит пользователь с запрошенными входящими заявками")
  void shouldResolveRequestedIncomeInvitations() {
    assertEquals(
        "1",
        published(Observed.class).get("incomeInvitations"),
        "Resolved user did not carry the requested income invitations"
    );
  }

  @Test
  @DisplayName("outcomeInvitations = 1: заявка PENDING есть во время теста и удаляется вместе с профилем приглашённого")
  void shouldRemoveOutcomeInvitationWithInviteeAfterTest() {
    Map<String, String> entries = published(OutcomeInvitation.class);
    assertEquals("true", entries.get("outcomeInvitation"), "Pending outcome invitation did not exist during the test");
    assertFalse(
        new Friendship(entries.get("username"), entries.get("invitee"), FriendshipStatus.PENDING).exists(),
        "Outcome invitation was not removed after the test"
    );
    assertFalse(
        new Profile(entries.get("invitee")).exists(),
        "Invitee profile was not removed after the test"
    );
  }

  @Test
  @DisplayName("После теста пользователя нет в auth")
  void shouldRemoveAccountAfterTest() {
    assertFalse(
        new AuthAccount(new TestUser(published(Observed.class).get("username"), "closed")).exists(),
        "Account was not removed after the test"
    );
  }

  @Test
  @DisplayName("После теста профиля нет в api")
  void shouldRemoveProfileAfterTest() {
    assertFalse(
        new Profile(published(Observed.class).get("username")).exists(),
        "Profile was not removed after the test"
    );
  }

  @Test
  @DisplayName("После теста профиля друга нет в api")
  void shouldRemoveFriendProfileAfterTest() {
    assertFalse(
        new Profile(published(Observed.class).get("friend")).exists(),
        "Friend profile was not removed after the test"
    );
  }

  @Test
  @DisplayName("После теста профиля пригласившего нет в api")
  void shouldRemoveInviterProfileAfterTest() {
    assertFalse(
        new Profile(published(Observed.class).get("inviter")).exists(),
        "Inviter profile was not removed after the test"
    );
  }

  @Test
  @DisplayName("После упавшего теста профиля нет в api")
  void shouldRemoveProfileAfterFailedTest() {
    assertFalse(
        new Profile(published(FailingCase.class).get("username")).exists(),
        "Profile was not removed after the failed test"
    );
  }

  @Test
  @DisplayName("Два параметра @User получают разных пользователей")
  void shouldResolveDistinctUsersForEachParameter() {
    Map<String, String> entries = published(TwoUsers.class);
    assertNotEquals(entries.get("first"), entries.get("second"), "Two @User parameters got the same user");
  }

  @Test
  @DisplayName("Отрицательное число друзей валит тест до создания пользователя")
  void shouldFailWhenFriendsCountIsNegative() {
    assertInstanceOf(
        IllegalArgumentException.class,
        failure(NegativeFriends.class),
        "Negative friends count did not fail the test with IllegalArgumentException"
    );
  }

  @Test
  @DisplayName("Отрицательное число входящих заявок валит тест до создания пользователя")
  void shouldFailWhenIncomeInvitationsCountIsNegative() {
    assertInstanceOf(
        IllegalArgumentException.class,
        failure(NegativeIncomeInvitations.class),
        "Negative income invitations count did not fail the test with IllegalArgumentException"
    );
  }

  @Test
  @DisplayName("outcomeInvitations = -1 валит тест с IllegalArgumentException до создания пользователя")
  void shouldFailWhenOutcomeInvitationsCountIsNegative() {
    Throwable thrown = failure(NegativeOutcomeInvitations.class);
    assertInstanceOf(
        IllegalArgumentException.class,
        thrown,
        "Negative outcome invitations count did not fail the test with IllegalArgumentException"
    );
    assertEquals(
        "Expected non-negative outcomeInvitations, got -1",
        thrown.getMessage(),
        "Negative outcome invitations count failed after the user could have been created"
    );
  }

  @Test
  @DisplayName("TestUser без @User не резолвится")
  void shouldRejectTestUserWithoutUserAnnotation() {
    assertInstanceOf(
        ParameterResolutionException.class,
        failure(UnannotatedUser.class),
        "TestUser without @User was resolved"
    );
  }

  @Test
  @DisplayName("@User в параметре @BeforeEach не резолвится")
  void shouldRejectUserOnBeforeEach() {
    assertInstanceOf(
        ParameterResolutionException.class,
        failure(BeforeEachUser.class),
        "@User in @BeforeEach parameter was resolved"
    );
  }

  private EngineExecutionResults execute(Class<?> type) {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
        .selectors(selectClass(type))
        .execute();
  }

  private Map<String, String> published(Class<?> type) {
    Map<String, String> entries = new HashMap<>();
    execute(type).allEvents().reportingEntryPublished().stream()
        .map(item -> item.getPayload(ReportEntry.class).orElseThrow())
        .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
    return entries;
  }

  private Throwable failure(Class<?> type) {
    return execute(type).testEvents().failed().stream()
        .map(item -> item.getPayload(TestExecutionResult.class).orElseThrow())
        .flatMap(result -> result.getThrowable().stream())
        .findFirst()
        .orElseThrow();
  }

  @ProbeTarget
  static final class Observed {
    @Test
    void observe(TestReporter reporter, @User(friends = 1, incomeInvitations = 1) TestUser user) {
      TestUser friend = user.friends().getFirst();
      TestUser inviter = user.incomeInvitations().getFirst();
      reporter.publishEntry(Map.of(
          "username", user.username(),
          "friend", friend.username(),
          "inviter", inviter.username(),
          "friends", String.valueOf(user.friends().size()),
          "incomeInvitations", String.valueOf(user.incomeInvitations().size())
      ));
      reporter.publishEntry(Map.of(
          "account", String.valueOf(new AuthAccount(user).exists()),
          "profile", String.valueOf(new Profile(user.username()).exists()),
          "friendship", String.valueOf(
              new Friendship(user.username(), friend.username(), FriendshipStatus.ACCEPTED).exists()
          ),
          "invitation", String.valueOf(
              new Friendship(inviter.username(), user.username(), FriendshipStatus.PENDING).exists()
          )
      ));
    }
  }

  @ProbeTarget
  static final class OutcomeInvitation {
    @Test
    void observe(TestReporter reporter, @User(outcomeInvitations = 1) TestUser user) {
      TestUser invitee = user.outcomeInvitations().getFirst();
      reporter.publishEntry(Map.of(
          "username", user.username(),
          "invitee", invitee.username(),
          "outcomeInvitation", String.valueOf(
              new Friendship(user.username(), invitee.username(), FriendshipStatus.PENDING).exists()
          )
      ));
    }
  }

  @ProbeTarget
  static final class FailingCase {
    @Test
    void fail(TestReporter reporter, @User TestUser user) {
      reporter.publishEntry("username", user.username());
      throw new IllegalStateException("Expected failure after user was published");
    }
  }

  @ProbeTarget
  static final class TwoUsers {
    @Test
    void observe(TestReporter reporter, @User TestUser first, @User TestUser second) {
      reporter.publishEntry(Map.of("first", first.username(), "second", second.username()));
    }
  }

  @ProbeTarget
  static final class NegativeFriends {
    @Test
    void observe(@User(friends = -1) TestUser user) {
    }
  }

  @ProbeTarget
  static final class NegativeIncomeInvitations {
    @Test
    void observe(@User(incomeInvitations = -1) TestUser user) {
    }
  }

  @ProbeTarget
  static final class NegativeOutcomeInvitations {
    @Test
    void observe(@User(outcomeInvitations = -1) TestUser user) {
    }
  }

  @ProbeTarget
  static final class UnannotatedUser {
    @Test
    void observe(TestUser user) {
    }
  }

  @ProbeTarget
  static final class BeforeEachUser {
    @BeforeEach
    void prepare(@User TestUser user) {
    }

    @Test
    void observe() {
    }
  }
}
