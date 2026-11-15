package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PhotoCard {

  private final Locator card;

  public PhotoCard(Locator card) {
    this.card = card;
  }

  @Step("Check that photo card is shown")
  public PhotoCard checkThatShown() {
    assertThat(card).isVisible();
    return this;
  }

  @Step("Check that Photo actions is shown")
  public PhotoCard checkThatActions() {
    assertThat(actions()).isVisible();
    return this;
  }

  @Step("Check that Photo actions is absent")
  public PhotoCard checkThatActionsAbsent() {
    assertThat(actions()).hasCount(0);
    return this;
  }

  @Step("Check that like is shown")
  public PhotoCard checkThatLike() {
    assertThat(like()).isVisible();
    return this;
  }

  private Locator actions() {
    return card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Photo actions").setExact(true));
  }

  private Locator like() {
    return card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("like").setExact(true));
  }
}
