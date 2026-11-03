package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Web")
@Feature("Auth")
@Story("Login")
@Tag("web")
class LoginNegativeTest extends BaseWebTest {

  private static final String BAD_CREDENTIALS = "Bad credentials";
  private static final String WRONG_PASSWORD = "WrongPass1!";

  @Test
  @DisplayName("Wrong password shows Bad credentials")
  void shouldShowBadCredentialsForWrongPassword(@User TestUser user, Page page) {
    LoginPage loginPage = new LandingPage(page)
        .open()
        .clickLogin();
    loginPage.fillLoginForm(user.username(), WRONG_PASSWORD)
        .clickSignIn();
    loginPage.checkThatErrorShown(BAD_CREDENTIALS);
  }

  @Test
  @DisplayName("Unknown username shows Bad credentials")
  void shouldShowBadCredentialsForUnknownUser(Page page) {
    TestUser unknown = TestUser.unique();
    LoginPage loginPage = new LandingPage(page)
        .open()
        .clickLogin();
    loginPage.fillLoginForm(unknown.username(), unknown.password())
        .clickSignIn();
    loginPage.checkThatErrorShown(BAD_CREDENTIALS);
  }
}
