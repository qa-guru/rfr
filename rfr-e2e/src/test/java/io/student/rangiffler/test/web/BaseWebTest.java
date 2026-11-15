package io.student.rangiffler.test.web;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.playwright.AllurePlaywright;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.MyTravelsPage;
import io.student.rangiffler.ui.page.PeoplePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@UiTest
public abstract class BaseWebTest {

  @BeforeAll
  static void setUp() {
    PlaywrightAssertions.setDefaultAssertionTimeout(TestConfig.assertionTimeout());
  }

  @BeforeEach
  void preparePage(Page page) {
    page.setDefaultTimeout(TestConfig.actionTimeout());//click, fill, locator.waitFor
    page.setDefaultNavigationTimeout(TestConfig.navigateTimeout());
    //navigate/goto, reload, goBack, goForward, waitForURL, waitForNavigation, setContent
  }

  @AfterEach
  void addAttachments(Page page) {
    if (page == null || page.isClosed()) {
      return;
    }
    AllurePlaywright.attachScreenshot("Last screenshot", page);
    AllurePlaywright.attachPageSource("Page source", page);
    AllurePlaywright.attachConsoleMessages("Browser console logs", page);
  }

  protected final MyTravelsPage openMyTravels(Page page, TestUser user) {
    return new LandingPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn();
  }

  protected final PeoplePage openPeople(Page page, TestUser user) {
    return new LandingPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .openPeople();
  }
}
