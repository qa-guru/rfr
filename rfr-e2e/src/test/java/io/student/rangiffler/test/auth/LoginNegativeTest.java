package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(LoginNegativeTest.EnglishOptions.class)
@DisplayName("Негативный вход")
class LoginNegativeTest {
    private static final String BAD_CREDENTIALS = "Bad credentials";

    public static class EnglishOptions implements OptionsFactory {
        @Override
        public Options getOptions() {
            return new Options()
                .setContextOptions(new Browser.NewContextOptions().setLocale("en-US"));
        }
    }

    @Test
    @DisplayName("Неверный пароль показывает Bad credentials")
    void wrongPasswordShowsBadCredentials(Page page, @User TestUser user) {
        new LandingPage(page).open()
            .clickLogin()
            .fillLoginForm(user.username(), user.password() + "-wrong")
            .clickSignInWithInvalidCredentials()
            .checkThatError(BAD_CREDENTIALS);
    }

    @Test
    @DisplayName("Несуществующий пользователь показывает Bad credentials")
    void unknownUserShowsBadCredentials(Page page) {
        TestUser unknown = TestUser.unique();
        new LandingPage(page).open()
            .clickLogin()
            .fillLoginForm(unknown.username(), unknown.password())
            .clickSignInWithInvalidCredentials()
            .checkThatError(BAD_CREDENTIALS);
    }
}