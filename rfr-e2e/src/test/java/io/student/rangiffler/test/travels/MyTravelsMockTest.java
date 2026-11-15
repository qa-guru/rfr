package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.test.web.BaseWebTest;
import io.student.rangiffler.ui.page.MyTravelsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Web")
@Feature("Travels")
@Story("Mock feed")
@Tag("web")
@DisplayName("My Travels on the mock feed")
class MyTravelsMockTest extends BaseWebTest {

  private static final int MY_PHOTOS = 4;
  private static final int MY_COUNTRIES = 4;
  private static final int FRIENDS_PHOTOS = 7;
  private static final int FRIENDS_COUNTRIES = 7;
  private static final String FILTER_COUNTRY = "Netherlands";
  private static final String OWN_PHOTO = "Амстердам!";
  private static final String FRIEND_PHOTO = "Старая Прага завораживает!";

  @Test
  @DisplayName("Toggle changes card count and countries visited on the mock feed")
  void shouldChangeCardCountOnMockFeed(@User TestUser user, Page page) {
    openMyTravels(page, user)
        .checkThatCardCount(MY_PHOTOS)
        .checkThatCountriesVisited(MY_COUNTRIES)
        .clickWithFriends()
        .checkThatCardCount(FRIENDS_PHOTOS)
        .checkThatCountriesVisited(FRIENDS_COUNTRIES)
        .clickOnlyMyTravels()
        .checkThatCardCount(MY_PHOTOS)
        .checkThatCountriesVisited(MY_COUNTRIES);
  }

  @Test
  @DisplayName("Country filter shows one card on the mock feed")
  void shouldFilterFeedByCountryOnMockFeed(@User TestUser user, Page page) {
    MyTravelsPage travels = openMyTravels(page, user)
        .checkThatCardCount(MY_PHOTOS)
        .clickCountriesVisited(MY_COUNTRIES)
        .selectCountry(FILTER_COUNTRY)
        .checkThatCountryFilter(FILTER_COUNTRY)
        .checkThatCardCount(1);
    travels.card(OWN_PHOTO).checkThatShown();
    travels.clickResetCountryFilter().checkThatCardCount(MY_PHOTOS);
  }

  @Test
  @DisplayName("Photo actions is only on own photos on the mock feed")
  void shouldShowPhotoActionsOnlyOnOwnPhotosOnMockFeed(@User TestUser user, Page page) {
    MyTravelsPage travels = openMyTravels(page, user);
    travels.card(OWN_PHOTO).checkThatActions();
    travels.clickWithFriends().checkThatCardCount(FRIENDS_PHOTOS);
    travels.card(OWN_PHOTO).checkThatActions();
    travels.card(FRIEND_PHOTO).checkThatActionsAbsent();
  }
}
