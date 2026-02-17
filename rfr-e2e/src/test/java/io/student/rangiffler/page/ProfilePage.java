package io.student.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;

@ParametersAreNonnullByDefault
public class ProfilePage {
    private final SelenideElement inputFirstName = $("#firstname");
    private final SelenideElement inputSurName= $("#surname");
    private final SelenideElement buttonSave = $(byText("Save"));


    @Step("Изменить имя и фамилию: {firstName} {surName}")
    @Nonnull
    public ProfilePage changeFirstNameAndSureName(String firstName, String surName) {
        changeFirstName(firstName);
        changeSureName(surName);
        return this;
    }

    @Step("Изменить имя на {firstName}")
    @Nonnull
    public ProfilePage changeFirstName(String firstName) {
        inputFirstName.shouldBe(visible).setValue(firstName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    @Step("Изменить фамилию на {surName}")
    @Nonnull
    public ProfilePage changeSureName(String surName) {
        inputSurName.shouldBe(visible).setValue(surName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    @Step("Перезагрузить страницу профиля")
    @Nonnull
    public ProfilePage reloadPage() {
        refresh();
        return this;
    }

    @Step("Проверить имя {expectedFirstName}")
    @Nonnull
    public ProfilePage shouldHaveFirstName(String expectedFirstName) {
        inputFirstName.shouldHave(value(expectedFirstName));
        return this;
    }

    @Step("Проверить фамилию {expectedSurName}")
    @Nonnull
    public ProfilePage shouldHaveSurName(String expectedSurName) {
        inputSurName.shouldHave(value(expectedSurName));
        return this;
    }

    @Step("Проверить полное имя {expectedFirstName} {expectedSurName}")
    @Nonnull
    public ProfilePage shouldHaveFullName(String expectedFirstName, String expectedSurName) {
        shouldHaveFirstName(expectedFirstName);
        shouldHaveSurName(expectedSurName);
        return this;
    }

}
