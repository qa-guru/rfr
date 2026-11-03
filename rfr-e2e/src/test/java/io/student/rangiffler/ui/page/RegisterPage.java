package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class RegisterPage {

  public static final String URL = TestConfig.authUrl() + "/register";

  private final Page page;
  private final CredentialsForm credentialsForm;
  private final Locator confirmPasswordInput;
  private final Locator signUpButton;
  private final Locator signInLink;

  public RegisterPage(Page page) {
    this.page = page;
    this.credentialsForm = new CredentialsForm(page);
    this.confirmPasswordInput = page.getByLabel("Confirm password", new Page.GetByLabelOptions().setExact(true));
    this.signUpButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up").setExact(true));
    this.signInLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Fill registration form: {username}")
  public RegisterPage fillRegistrationForm(String username, String password) {
    credentialsForm.fillCredentials(username, password);
    confirmPasswordInput.fill(password);
    signUpButton.click();
    return this;
  }

  @Step("Check that success message is shown: {message}")
  public RegisterPage checkThatSuccessMessage(String message) {
    assertThat(page.getByText(message, new Page.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }

  @Step("Go to frontend from registration success")
  public LandingPage clickSignIn() {
    signInLink.click();
    return new LandingPage(page);
  }
}
