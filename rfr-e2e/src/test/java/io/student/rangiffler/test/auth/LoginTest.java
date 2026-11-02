package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Web")
@Feature("Auth")
@Story("Login")
@Tag("web")
class LoginTest extends BaseWebTest {

  @Test
  @DisplayName("Existing user can authorize into the cabinet")
  void shouldAuthorizeExistingUser(@User TestUser user, Page page) {
    new LandingPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .openProfile()
        .checkThatUsername(user.username());
  }
}
