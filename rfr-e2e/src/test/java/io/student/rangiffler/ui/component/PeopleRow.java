package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeopleRow {

  private final Page page;
  private final Locator row;

  public PeopleRow(Page page, Locator row) {
    this.page = page;
    this.row = row;
  }

  @Step("Add person")
  public PeopleRow clickAdd() {
    waitForGraphql("FriendshipAction", () -> button("Add").click());
    return this;
  }

  @Step("Check that username is shown")
  public PeopleRow checkThatUsername() {
    assertThat(row).isVisible();
    return this;
  }

  @Step("Check that username is absent")
  public PeopleRow checkThatUsernameAbsent() {
    assertThat(row).hasCount(0);
    return this;
  }

  @Step("Check that Remove is shown")
  public PeopleRow checkThatRemove() {
    assertThat(button("Remove")).isVisible();
    return this;
  }

  @Step("Check that Accept is shown")
  public PeopleRow checkThatAccept() {
    assertThat(button("Accept")).isVisible();
    return this;
  }

  @Step("Check that Decline is shown")
  public PeopleRow checkThatDecline() {
    assertThat(button("Decline")).isVisible();
    return this;
  }

  @Step("Check that Cancel is shown")
  public PeopleRow checkThatCancel() {
    assertThat(button("Cancel")).isVisible();
    return this;
  }

  @Step("Check that invitation is waiting")
  public PeopleRow checkThatInvitationWaiting() {
    assertThat(row.getByText("Waiting...", new Locator.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }

  private Locator button(String name) {
    return row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(name).setExact(true));
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
