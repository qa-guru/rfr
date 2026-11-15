package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeopleTable {

  private final Page page;
  private final Locator searchInput;
  private final Locator searchButton;
  private final Locator removeButtons;
  private final Locator acceptButtons;
  private final Locator emptyUsers;

  public PeopleTable(Page page) {
    this.page = page;
    this.searchInput = page.getByRole(
        AriaRole.TEXTBOX,
        new Page.GetByRoleOptions().setName("search people").setExact(true)
    );
    this.searchButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("search").setExact(true));
    this.removeButtons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Remove").setExact(true));
    this.acceptButtons = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept").setExact(true));
    this.emptyUsers = page.getByRole(AriaRole.TABPANEL).getByRole(AriaRole.PARAGRAPH);
  }

  @Step("People row: {username}")
  public PeopleRow row(String username) {
    return new PeopleRow(
        page,
        page.getByTestId("people-row").filter(new Locator.FilterOptions().setHasText(username))
    );
  }

  @Step("Search people: {query}")
  public PeopleTable searchPeople(String query) {
    searchInput.fill(query);
    waitForGraphql("GetPeople", searchButton::click);
    return this;
  }

  @Step("Check that remove count is {count}")
  public PeopleTable checkThatRemoveCount(int count) {
    assertThat(removeButtons).hasCount(count);
    return this;
  }

  @Step("Check that accept count is {count}")
  public PeopleTable checkThatAcceptCount(int count) {
    assertThat(acceptButtons).hasCount(count);
    return this;
  }

  @Step("Check that people list is empty: {message}")
  public PeopleTable checkThatNoUsers(String message) {
    assertThat(emptyUsers).hasText(message);
    return this;
  }

  private void waitForGraphql(String operation, Runnable click) {
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains(operation),
        click
    );
  }
}
