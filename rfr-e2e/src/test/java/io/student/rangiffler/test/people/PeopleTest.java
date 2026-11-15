package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Invitations;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.PeopleTab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Web")
@Feature("People")
@Story("Friendship preconditions")
class PeopleTest extends BaseWebTest {

  @Test
  @DisplayName("Friends and income invitations from @User are visible")
  void shouldShowFriendsAndIncomeInvitationsFromAnnotation(
      @User(friends = 2, incomeInvitations = 1) TestUser user,
      Page page
  ) {
    assertEquals(2, new Friendships(user.username()).accepted(), "Expected two accepted friendships for " + user.username());
    assertEquals(1, new Invitations(user.username()).count(), "Expected one pending invitation for " + user.username());
    openPeople(page, user)
        .checkThatRemoveCount(2)
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .checkThatAcceptCount(1);
  }

  @Test
  @DisplayName("Invitation created in browser is deleted when the user store closes")
  void shouldDeleteInvitationCreatedInBrowser(
      @User TestUser user,
      @User TestUser other,
      Page page
  ) {
    Invitations invitations = new Invitations(user.username());
    assertEquals(0, invitations.count(), "Expected no pending invitations before UI action for " + user.username());
    openPeople(page, user)
        .openTab(PeopleTab.ALL_PEOPLE)
        .searchPeople(other.username())
        .clickAdd(other.username())
        .checkThatInvitationWaiting(other.username());
    assertEquals(1, invitations.count(), "Expected one pending invitation after Add for " + user.username());
  }
}
