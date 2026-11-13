package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.ProfilePage;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Base64;

@UsePlaywright
@DisplayName("Профиль")
class ProfileTest {
    private static final String FIRST_NAME = "Anna";
    private static final String OTHER_FIRST_NAME = "Maria";
    private static final String SURNAME = "Smith";
    private static final String OTHER_SURNAME = "Brown";
    private static final String COUNTRY = "France";
    private static final String OTHER_COUNTRY = "Germany";

    @Test
    @DisplayName("Сохранённое имя видно после перезагрузки")
    void savedFirstNameStaysAfterReload(Page page, @User TestUser user) {
        openProfile(page, user)
            .fillFirstName(FIRST_NAME)
            .clickSave()
            .reload()
            .checkThatFirstName(FIRST_NAME);
    }

    @Test
    @DisplayName("Сохранённая фамилия видна после перезагрузки")
    void savedSurnameStaysAfterReload(Page page, @User TestUser user) {
        openProfile(page, user)
            .fillSurname(SURNAME)
            .clickSave()
            .reload()
            .checkThatSurname(SURNAME);
    }

    @Test
    @DisplayName("Сохранённая страна видна после перезагрузки")
    void savedCountryStaysAfterReload(Page page, @User TestUser user) {
        openProfile(page, user)
            .selectLocation(COUNTRY)
            .clickSave()
            .reload()
            .checkThatLocation(COUNTRY);
    }

    @Test
    @DisplayName("Сохранённый аватар виден после перезагрузки")
    void savedAvatarStaysAfterReload(Page page, @User TestUser user) {
        Path avatar = avatar();
        openProfile(page, user)
            .uploadAvatar(avatar)
            .clickSave()
            .reload()
            .checkThatAvatarSaved(avatarDataUrl(avatar));
    }

    private static String avatarDataUrl(Path avatar) {
        try {
            String encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(avatar));
            return "data:image/png;base64," + encoded;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + avatar, e);
        }
    }

    @Test
    @DisplayName("Reset возвращает сохранённые значения")
    void resetRestoresSavedValues(Page page, @User TestUser user) {
        ProfilePage profile = openProfile(page, user)
            .fillFirstName(FIRST_NAME)
            .fillSurname(SURNAME)
            .selectLocation(COUNTRY)
            .clickSave()
            .reload();

        profile.checkThatFirstName(FIRST_NAME)
            .checkThatSurname(SURNAME)
            .checkThatLocation(COUNTRY)
            .fillFirstName(OTHER_FIRST_NAME)
            .fillSurname(OTHER_SURNAME)
            .selectLocation(OTHER_COUNTRY)
            .clickReset()
            .checkThatFirstName(FIRST_NAME)
            .checkThatSurname(SURNAME)
            .checkThatLocation(COUNTRY);
    }

    private static ProfilePage openProfile(Page page, TestUser user) {
        return new LandingPage(page).open()
            .clickLogin()
            .fillLoginForm(user.username(), user.password())
            .clickSignIn()
            .clickProfile();
    }

    private static Path avatar() {
        try {
            return Path.of(Objects.requireNonNull(
                ProfileTest.class.getResource("/images/avatar.png"),
                "avatar.png").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot read avatar.png", e);
        }
    }
}