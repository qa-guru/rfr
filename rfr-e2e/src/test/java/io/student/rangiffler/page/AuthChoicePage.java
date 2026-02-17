package io.student.rangiffler.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class AuthChoicePage {
    private final SelenideElement loginBtn = $(byText("Login"));
    private final SelenideElement registerBtn = $(byText("Register"));

    @Step("Открыть форму логина")
    @Nonnull
    public LoginPage clickLogin() {
        loginBtn.click();
        return Selenide.page(LoginPage.class);
    }

    @Step("Открыть форму регистрации")
    @Nonnull
    public RegisterPage clickRegister() {
        registerBtn.click();
        return Selenide.page(RegisterPage.class);
    }
}
