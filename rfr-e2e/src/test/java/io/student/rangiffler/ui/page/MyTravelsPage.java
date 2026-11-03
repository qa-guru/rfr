package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.HeaderComponent;

public final class MyTravelsPage {

  public static final String URL = TestConfig.frontUrl() + "/my-travels";

  private final Page page;
  private final HeaderComponent header;

  public MyTravelsPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
  }

  @Step("Open profile")
  public ProfilePage openProfile() {
    return header.openProfile();
  }
}
