package io.student.rangiffler.test.web;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Web")
@Feature("Auth")
@Story("Registration")
@Tag("web")
class RegistrationTest extends BaseWebTest {

  private UserFixture userFixture;

  @Test
  @DisplayName("User can register and then authorize into the cabinet")
  void shouldAuthorizeAfterRegistration(Page page) {
    TestUser user = TestUser.unique();
    userFixture = new UserFixture(user);
    new LandingPage(page)
        .open()
        .clickRegister()
        .fillRegistrationForm(user.username(), user.password())
        .checkThatSuccessMessage("Congratulations! You've registered!")
        .clickSignIn()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .openProfile()
        .checkThatUsername(user.username());
  }

  @AfterEach
  void deleteUser() {
    if (userFixture == null) {
      return;
    }
    userFixture.close();
  }
}
