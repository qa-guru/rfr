package io.student.rangiffler.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;

public class ProfilePage {
    private final SelenideElement inputFirstName = $("#firstname");
    private final SelenideElement inputSurName= $("#surname");
    private final SelenideElement buttonSave = $(byText("Save"));


    public ProfilePage changeFirstNameAndSureName(String firstName, String surName) {
        changeFirstName(firstName);
        changeSureName(surName);
        return this;
    }

    public ProfilePage changeFirstName(String firstName) {
        inputFirstName.shouldBe(visible).setValue(firstName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    public ProfilePage changeSureName(String surName) {
        inputSurName.shouldBe(visible).setValue(surName);
        buttonSave.shouldBe(visible).click();
        return this;
    }

    public ProfilePage reloadPage() {
        refresh();
        return this;
    }

    public ProfilePage shouldHaveFirstName(String expectedFirstName) {
        inputFirstName.shouldHave(value(expectedFirstName));
        return this;
    }

    public ProfilePage shouldHaveSurName(String expectedSurName) {
        inputSurName.shouldHave(value(expectedSurName));
        return this;
    }

    public ProfilePage shouldHaveFullName(String expectedFirstName, String expectedSurName) {
        shouldHaveFirstName(expectedFirstName);
        shouldHaveSurName(expectedSurName);
        return this;
    }

}
