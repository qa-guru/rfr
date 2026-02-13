package io.student.rangiffler;


import io.student.rangiffler.config.Config;
import io.student.rangiffler.jupiter.annotation.Photo;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.page.AuthChoicePage;
import io.student.rangiffler.page.RegistrationResult;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.*;
import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;

@ExtendWith(UserExtension.class)
public class AuthTests {

    private static final Config CFG = Config.getInstance();
    private static final Faker faker = new Faker();

    @AfterEach
    public void afterEach() {
        clearBrowserLocalStorage();
        clearBrowserCookies();
        closeWebDriver();

    }

    @Test
    @DisplayName("Регистрация нового пользователя с валидными данными")
    public void shouldRegisterNewUser() {
        String userName = faker.name().username();
        String password = faker.internet().password();
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickRegister()
                .registerUser(userName, password, password)
                .checkRegistrationResult()
                .successRegisterBtnClick()
                .clickLogin()
                .login(userName, password)
                .shouldBeVisibleMap();
    }

    @User
    @Test
    @DisplayName("Регистрация не должна проходить, если пользователь с таким логином уже существует")
    public void shouldNotRegisterUserWithExistingUsername(UserJson userJson) {
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickRegister()
                .registerUser(userJson.data().user().username(), STANDART_PASSWORD, STANDART_PASSWORD)
                .checkRegistrationResultNotSuccessMessage();
    }

    @Test
    @DisplayName("Отображается ошибка, если пароль и подтверждение пароля не совпадают")
    public void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        String userName = faker.name().username();
        String password = faker.internet().password();
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickRegister()
                .registerUser(userName, password, STANDART_PASSWORD)
                .checkRegistrationFail(RegistrationResult.PASSWORD_NOT_EQUAL);
    }

    @User(
            photos = @Photo(
                    description = "bla bla bla"
            )

    )
    @Test
    @DisplayName("После успешного логина отображается главная страница")
    public void mainPageShouldBeDisplayedAfterSuccessLogin(UserJson userJson) {
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(userJson.data().user().username(), STANDART_PASSWORD)
                .shouldBeVisibleMap();
    }

    @Test
    @DisplayName("При вводе неверных учетных данных пользователь остается на странице логина")
    public void userShouldStayOnLoginPageAfterLoginWithBadCredentials() {
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(faker.name().username(), faker.internet().password())
                .shouldNotVisibleMap();
    }
}