package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

public final class CredentialsForm {

  private final Page page;
  private final Locator usernameInput;
  private final Locator passwordInput;

  public CredentialsForm(Page page) {
    this.page = page;
    this.usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true));
    this.passwordInput = page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true));
  }

  @Step("Fill credentials: {username}")
  public CredentialsForm fillCredentials(String username, String password) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    return this;
  }
}
