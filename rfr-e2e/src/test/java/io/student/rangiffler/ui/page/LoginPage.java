package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.CredentialsForm;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class LoginPage {
  public static final String URL = TestConfig.authUrl() + "/login";
  private final Page page;
  private final CredentialsForm credentials;
  private final Locator signIn;
  private final Locator error;

  public LoginPage(Page page) {
    this.page = page;
    this.credentials = new CredentialsForm(page);
    this.signIn = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Sign in").setExact(true));
    this.error = page.locator("form.form .form__error");
  }

  @Step("Заполнить форму входа: {username} / {password}")
  public LoginPage fillLoginForm(String username, String password) {
    credentials.fillCredentials(username, password);
    return this;
  }

  @Step("Нажать Sign in")
  public MainPage clickSignIn() {
    signIn.click();
    return new MainPage(page);
  }

  @Step("Нажать Sign in с неверными данными")
  public LoginPage clickSignInWithInvalidCredentials() {
    signIn.click();
    page.waitForURL(url -> url.contains("/login") && url.contains("error"));
    return this;
  }

  @Step("Проверить сообщение об ошибке: {message}")
  public LoginPage checkThatError(String message) {
    assertThat(error).containsText(message);
    return this;
  }
}