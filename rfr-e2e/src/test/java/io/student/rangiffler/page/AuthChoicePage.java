package io.student.rangiffler.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class AuthChoicePage {
    private final SelenideElement loginBtn = $(byText("Login"));
    private final SelenideElement registerBtn = $(byText("Register"));

    @Step("Открыть форму логина")
    public LoginPage clickLogin() {
        loginBtn.click();
        return Selenide.page(LoginPage.class);
    }

    @Step("Открыть форму регистрации")
    public RegisterPage clickRegister() {
        registerBtn.click();
        return Selenide.page(RegisterPage.class);
    }
}
