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
@Story("Income invitations")
class IncomeInvitationsTabTest extends BaseWebTest {

  @Test
  @DisplayName("Income invitations tab shows Accept and Decline and hides the friend")
  void shouldShowIncomeInvitationAndHideFriend(
      @User(friends = 1, incomeInvitations = 1) TestUser user,
      Page page
  ) {
    TestUser friend = user.friends().getFirst();
    TestUser inviter = user.incomeInvitations().getFirst();
    openPeople(page, user)
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .checkThatUsername(inviter.username())
        .checkThatAccept(inviter.username())
        .checkThatDecline(inviter.username())
        .checkThatUsernameAbsent(friend.username());
  }

  @Test
  @DisplayName("Income invitations tab is empty without invitations")
  void shouldShowEmptyIncomeInvitations(@User TestUser user, Page page) {
    openPeople(page, user)
        .openTab(PeopleTab.INCOME_INVITATIONS)
        .checkThatNoUsers("There are no users yet");
  }
}
