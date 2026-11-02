package io.student.rangiffler.test.web;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.playwright.AllurePlaywright;
import io.student.rangiffler.jupiter.annotation.WebTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

@WebTest
public abstract class BaseWebTest {

  @BeforeAll
  static void setUp() {
    PlaywrightAssertions.setDefaultAssertionTimeout(15_000);
  }

  @BeforeEach
  void preparePage(Page page) {
    page.setDefaultTimeout(15_000);
    page.setDefaultNavigationTimeout(15_000);
  }

  @AfterEach
  void addAttachments(Page page) {
    if (page == null || page.isClosed()) {
      return;
    }
    AllurePlaywright.attachScreenshot("Last screenshot", page);
    AllurePlaywright.attachPageSource("Page source", page);
    AllurePlaywright.attachConsoleMessages("Browser console logs", page);
    page.context().close();
  }
}
