package io.student.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;

public class ProfilePage {
    private final SelenideElement inputFirstName = $("#firstname");
    private final SelenideElement inputSurName= $("#surname");
    private final SelenideElement buttonSave = $(byText("Save"));


    @Step("Изменить имя и фамилию: {firstName} {surName}")
    public ProfilePage changeFirstNameAndSureName(String firstName, String surName) {
        changeFirstName(firstName);
        changeSureName(surName);
        return this;
    }

    @Step("Изменить имя на {firstName}")
    public ProfilePage changeFirstName(String firstName) {
        inputFirstName.shouldBe(visible).setValue(firstName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    @Step("Изменить фамилию на {surName}")
    public ProfilePage changeSureName(String surName) {
        inputSurName.shouldBe(visible).setValue(surName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    @Step("Перезагрузить страницу профиля")
    public ProfilePage reloadPage() {
        refresh();
        return this;
    }

    @Step("Проверить имя {expectedFirstName}")
    public ProfilePage shouldHaveFirstName(String expectedFirstName) {
        inputFirstName.shouldHave(value(expectedFirstName));
        return this;
    }

    @Step("Проверить фамилию {expectedSurName}")
    public ProfilePage shouldHaveSurName(String expectedSurName) {
        inputSurName.shouldHave(value(expectedSurName));
        return this;
    }

    @Step("Проверить полное имя {expectedFirstName} {expectedSurName}")
    public ProfilePage shouldHaveFullName(String expectedFirstName, String expectedSurName) {
        shouldHaveFirstName(expectedFirstName);
        shouldHaveSurName(expectedSurName);
        return this;
    }

}
