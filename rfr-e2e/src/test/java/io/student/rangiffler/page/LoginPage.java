package io.student.rangiffler.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class LoginPage {

    private final SelenideElement usernameInput = $("[name='username']");
    private final SelenideElement passwordInput = $("[name='password']");
    private final SelenideElement submitBtn = $("[type='submit']");

    @Step("Выполнить логин пользователем {username}")
    @Nonnull
    public MapPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        return Selenide.page(MapPage.class);
    }
}
