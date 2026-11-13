package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
    public static final String URL = TestConfig.frontUrl() + "/profile";

    private final Page page;
    private final Locator usernameInput;
    private final Locator firstName;
    private final Locator surname;
    private final Locator location;
    private final Locator avatarInput;
    private final Locator savedAvatar;
    private final Locator save;
    private final Locator reset;

    public ProfilePage(Page page) {
        this.page = page;
        this.usernameInput = page.getByLabel("Username", new Page.GetByLabelOptions().setExact(true));
        this.firstName = page.getByLabel("First Name", new Page.GetByLabelOptions().setExact(true));
        this.surname = page.getByLabel("Surname", new Page.GetByLabelOptions().setExact(true));
        this.location = page.getByRole(AriaRole.COMBOBOX,
            new Page.GetByRoleOptions().setName("Location").setExact(true));
        this.avatarInput = page.getByLabel("Update avatar", new Page.GetByLabelOptions().setExact(true));
        this.savedAvatar = page.getByRole(AriaRole.MAIN)
            .locator("img[src^='data:image/']:not([alt]), img[alt=''][src^='data:image/']");
        this.save = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Save").setExact(true));
        this.reset = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Reset").setExact(true));
    }

    @Step("Открыть профиль")
    public ProfilePage open() {
        page.navigate(URL);
        return this;
    }

    @Step("Перезагрузить страницу профиля")
    public ProfilePage reload() {
        page.reload();
        return new ProfilePage(page);
    }

    @Step("Заполнить First Name: {firstName}")
    public ProfilePage fillFirstName(String firstName) {
        this.firstName.fill(firstName);
        return this;
    }

    @Step("Заполнить Surname: {surname}")
    public ProfilePage fillSurname(String surname) {
        this.surname.fill(surname);
        return this;
    }

    @Step("Выбрать страну: {country}")
    public ProfilePage selectLocation(String country) {
        location.click();
        page.getByRole(AriaRole.OPTION)
            .filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.IMG,
                    new Page.GetByRoleOptions().setName(country).setExact(true))))
            .click();
        return this;
    }

    @Step("Загрузить аватар: {avatar}")
    public ProfilePage uploadAvatar(Path avatar) {
        avatarInput.setInputFiles(avatar);
        savedAvatar.waitFor();
        return this;
    }

    @Step("Нажать Save")
    public ProfilePage clickSave() {
        page.waitForResponse(
            response -> {
                String body = response.request().postData();
                return body != null && body.contains("UpdateUser");
            },
            () -> save.click());
        return this;
    }

    @Step("Нажать Reset")
    public ProfilePage clickReset() {
        reset.click();
        return this;
    }

    @Step("Проверить имя пользователя в профиле: {username}")
    public ProfilePage checkThatUsername(String username) {
        assertThat(usernameInput).hasValue(username);
        return this;
    }

    @Step("Проверить First Name: {firstName}")
    public ProfilePage checkThatFirstName(String firstName) {
        assertThat(this.firstName).hasValue(firstName);
        return this;
    }

    @Step("Проверить Surname: {surname}")
    public ProfilePage checkThatSurname(String surname) {
        assertThat(this.surname).hasValue(surname);
        return this;
    }

    @Step("Проверить страну: {country}")
    public ProfilePage checkThatLocation(String country) {
        assertThat(location.getByRole(AriaRole.IMG,
            new Locator.GetByRoleOptions().setName(country).setExact(true))).isVisible();
        return this;
    }

  @Step("Проверить, что аватар сохранён: {avatarSrc}")
  public ProfilePage checkThatAvatarSaved(String avatarSrc) {
    assertThat(savedAvatar).hasAttribute("src", avatarSrc);
    return this;
  }
}