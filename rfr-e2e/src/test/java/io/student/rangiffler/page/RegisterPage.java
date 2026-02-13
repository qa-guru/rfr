package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class RegisterPage {
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement passwordSubmitInput = $("#passwordSubmit");
    private final SelenideElement submitBtn = $("[type='submit']");
    private final SelenideElement resultRegistrationForm = $(".form");
    private final SelenideElement successRegisterBtn = $(byText("Sign in!"));
    private final ElementsCollection labelForms = $$(".form__label");
    private final SelenideElement labelPasswordForm = labelForms.find(text("Password"));


    public RegisterPage registerUser(String userName, String password, String passwordSubmit) {
        usernameInput.sendKeys(userName);
        passwordInput.sendKeys(password);
        passwordSubmitInput.sendKeys(passwordSubmit);
        submitBtn.click();
        return this;
    }

    public AuthChoicePage successRegisterBtnClick() {
        successRegisterBtn.click();
        return Selenide.page(AuthChoicePage.class);
    }

    public RegisterPage checkRegistrationResult() {
        resultRegistrationForm.shouldHave(text(RegistrationResult.SUCCESS.getText()));
        return this;
    }

    public RegisterPage checkRegistrationResultNotSuccessMessage() {
        resultRegistrationForm.shouldNotHave(text(RegistrationResult.SUCCESS.getText()));
        return this;
    }

    public RegisterPage checkRegistrationFail(RegistrationResult registrationResult) {
        labelPasswordForm.shouldHave(text(registrationResult.getText()));
        return this;
    }
}
