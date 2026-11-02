package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.ProfilePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

@Epic("Web")
@Feature("Profile")
@Story("Edit profile")
@Tag("web")
class ProfileTest extends BaseWebTest {

  private static final String FIRST_NAME = "Ivan";
  private static final String SURNAME = "Petrov";
  private static final String COUNTRY = "France";
  private static final String OTHER_COUNTRY = "Germany";
  private static final String AVATAR_SRC_PREFIX = "data:image/";

  @Test
  @DisplayName("Each profile field is saved and visible after reload")
  void shouldSaveEachFieldAndReadAfterReload(@User TestUser user, Page page) {
    openProfile(page, user)
        .fillFirstName(FIRST_NAME)
        .clickSave()
        .reload()
        .checkThatFirstName(FIRST_NAME)
        .fillSurname(SURNAME)
        .clickSave()
        .reload()
        .checkThatSurname(SURNAME)
        .setAvatar(avatarFile())
        .clickSave()
        .reload()
        .checkThatAvatar(AVATAR_SRC_PREFIX);
  }

  @Test
  @DisplayName("Reset restores last saved profile values")
  void shouldResetToSavedValues(@User TestUser user, Page page) {
    openProfile(page, user)
        .fillFirstName(FIRST_NAME)
        .fillSurname(SURNAME)
        .selectLocation(COUNTRY)
        .clickSave()
        .reload()
        .fillFirstName("Dirty")
        .fillSurname("Dirty")
        .selectLocation(OTHER_COUNTRY)
        .clickReset()
        .checkThatFirstName(FIRST_NAME)
        .checkThatSurname(SURNAME)
        .checkThatLocation(COUNTRY);
  }

  @Test
  @DisplayName("Country change is saved and visible after reload")
  void shouldChangeCountryAndReadAfterReload(@User TestUser user, Page page) {
    openProfile(page, user)
        .selectLocation(COUNTRY)
        .clickSave()
        .reload()
        .checkThatLocation(COUNTRY);
  }

  private static ProfilePage openProfile(Page page, TestUser user) {
    return new LandingPage(page)
        .open()
        .clickLogin()
        .fillLoginForm(user.username(), user.password())
        .clickSignIn()
        .openProfile();
  }

  private static Path avatarFile() {
    URL resource = ProfileTest.class.getClassLoader().getResource("image/duck.jpg");
    if (resource == null) {
      throw new IllegalStateException("Classpath image/duck.jpg is missing");
    }
    try {
      return Path.of(resource.toURI());
    } catch (URISyntaxException exception) {
      throw new IllegalStateException("Classpath image/duck.jpg is not a file", exception);
    }
  }
}
