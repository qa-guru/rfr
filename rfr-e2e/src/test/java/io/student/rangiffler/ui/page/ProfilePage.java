package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.HeaderComponent;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {

  public static final String URL = TestConfig.frontUrl() + "/profile";

  private final Page page;
  private final HeaderComponent header;
  private final Locator firstNameInput;
  private final Locator surnameInput;
  private final Locator usernameInput;
  private final Locator locationCombobox;
  private final Locator avatarInput;
  private final Locator saveButton;
  private final Locator resetButton;

  public ProfilePage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
    this.firstNameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("First Name").setExact(true));
    this.surnameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Surname").setExact(true));
    this.usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true));
    this.locationCombobox = page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Location").setExact(true));
    this.avatarInput = page.getByLabel("Update avatar", new Page.GetByLabelOptions().setExact(true));
    this.saveButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save").setExact(true));
    this.resetButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Reset").setExact(true));
  }

  @Step("Fill first name: {firstName}")
  public ProfilePage fillFirstName(String firstName) {
    firstNameInput.fill(firstName);
    return this;
  }

  @Step("Fill surname: {surname}")
  public ProfilePage fillSurname(String surname) {
    surnameInput.fill(surname);
    return this;
  }

  @Step("Select location: {country}")
  public ProfilePage selectLocation(String country) {
    locationCombobox.click();
    page.getByRole(AriaRole.OPTION)
        .filter(new Locator.FilterOptions().setHas(
            page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName(country).setExact(true))
        ))
        .click();
    return this;
  }

  @Step("Set avatar: {image}")
  public ProfilePage setAvatar(Path image) {
    avatarInput.setInputFiles(image);
    return this;
  }

  @Step("Click save")
  public ProfilePage clickSave() {
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains("UpdateUser"),
        saveButton::click
    );
    return this;
  }

  @Step("Click reset")
  public ProfilePage clickReset() {
    resetButton.click();
    return this;
  }

  @Step("Reload profile")
  public ProfilePage reload() {
    page.reload();
    return this;
  }

  @Step("Check that first name is {firstName}")
  public ProfilePage checkThatFirstName(String firstName) {
    assertThat(firstNameInput).hasValue(firstName);
    return this;
  }

  @Step("Check that surname is {surname}")
  public ProfilePage checkThatSurname(String surname) {
    assertThat(surnameInput).hasValue(surname);
    return this;
  }

  @Step("Check that location is {country}")
  public ProfilePage checkThatLocation(String country) {
    assertThat(locationCombobox).containsText(country);
    return this;
  }

  @Step("Check that username is {username}")
  public ProfilePage checkThatUsername(String username) {
    assertThat(usernameInput).hasValue(username);
    return this;
  }

  @Step("Check that avatar src starts with {srcPrefix}")
  public ProfilePage checkThatAvatar(String srcPrefix) {
    assertThat(page.getByRole(AriaRole.MAIN).locator("img[src^='data:image/']"))
        .hasAttribute("src", Pattern.compile("^" + srcPrefix + ".*"));
    return this;
  }
}
