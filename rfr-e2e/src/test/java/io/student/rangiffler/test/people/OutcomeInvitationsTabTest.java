package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.PeopleTab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Web")
@Feature("People")
@Story("Outcome invitations")
class OutcomeInvitationsTabTest extends BaseWebTest {

  @Test
  @DisplayName("Outcome invitations tab shows Waiting and Cancel and hides other relations")
  void shouldShowOutcomeInvitationAndHideOtherRelations(
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user,
      Page page
  ) {
    TestUser friend = user.friends().getFirst();
    TestUser inviter = user.incomeInvitations().getFirst();
    TestUser invitee = user.outcomeInvitations().getFirst();
    openPeople(page, user)
        .openTab(PeopleTab.OUTCOME_INVITATIONS)
        .checkThatUsername(invitee.username())
        .checkThatInvitationWaiting(invitee.username())
        .checkThatCancel(invitee.username())
        .checkThatUsernameAbsent(friend.username())
        .checkThatUsernameAbsent(inviter.username());
  }

  @Test
  @DisplayName("Outcome invitations tab is empty without invitations")
  void shouldShowEmptyOutcomeInvitations(@User TestUser user, Page page) {
    openPeople(page, user)
        .openTab(PeopleTab.OUTCOME_INVITATIONS)
        .checkThatNoUsers("There are no users yet");
  }
}
