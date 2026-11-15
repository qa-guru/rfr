package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.ProfilePage;

public final class HeaderComponent {

  private final Page page;
  private final Locator profileLink;
  private final Locator peopleLink;

  public HeaderComponent(Page page) {
    this.page = page;
    this.profileLink = page.getByRole(AriaRole.BANNER)
        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Profile").setExact(true));
    this.peopleLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("People").setExact(true));
  }

  @Step("Open profile")
  public ProfilePage openProfile() {
    profileLink.click();
    return new ProfilePage(page);
  }

  @Step("Open people")
  public PeoplePage openPeople() {
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains("GetFriends"),
        peopleLink::click
    );
    return new PeoplePage(page);
  }
}
