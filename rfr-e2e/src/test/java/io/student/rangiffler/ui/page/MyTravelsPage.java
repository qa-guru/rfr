package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.HeaderComponent;
import io.student.rangiffler.ui.component.PhotoCard;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MyTravelsPage {

  public static final String URL = TestConfig.frontUrl() + "/my-travels";

  private final Page page;
  private final HeaderComponent header;
  private final Locator onlyMyTravels;
  private final Locator withFriends;

  public MyTravelsPage(Page page) {
    this.page = page;
    this.header = new HeaderComponent(page);
    this.onlyMyTravels = page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Only my travels").setExact(true)
    );
    this.withFriends = page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("With friends").setExact(true)
    );
  }

  @Step("Open profile")
  public ProfilePage openProfile() {
    return header.openProfile();
  }

  @Step("Open people")
  public PeoplePage openPeople() {
    return header.openPeople();
  }

  @Step("Show only my travels")
  public MyTravelsPage clickOnlyMyTravels() {
    waitForFeed(onlyMyTravels::click);
    return this;
  }

  @Step("Show travels with friends")
  public MyTravelsPage clickWithFriends() {
    waitForFeed(withFriends::click);
    return this;
  }

  @Step("Open visited countries list")
  public MyTravelsPage clickCountriesVisited(int count) {
    countriesVisited(count).click();
    return this;
  }

  @Step("Select country: {country}")
  public MyTravelsPage selectCountry(String country) {
    waitForFeed(
        () -> page.getByRole(AriaRole.TOOLTIP)
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(country).setExact(true))
            .click()
    );
    return this;
  }

  @Step("Reset country filter")
  public MyTravelsPage clickResetCountryFilter() {
    waitForFeed(
        () -> page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Reset country filter").setExact(true)
        ).click()
    );
    return this;
  }

  @Step("Photo card: {description}")
  public PhotoCard card(String description) {
    return new PhotoCard(
        page.getByRole(AriaRole.ARTICLE)
            .filter(new Locator.FilterOptions().setHasText(description))
    );
  }

  @Step("Check that photo card count is {count}")
  public MyTravelsPage checkThatCardCount(int count) {
    assertThat(page.getByRole(AriaRole.ARTICLE)).hasCount(count);
    return this;
  }

  @Step("Check that countries visited is {count}")
  public MyTravelsPage checkThatCountriesVisited(int count) {
    assertThat(countriesVisited(count)).hasText(countriesVisitedLabel(count));
    return this;
  }

  @Step("Check that country filter is {country}")
  public MyTravelsPage checkThatCountryFilter(String country) {
    assertThat(
        page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Country filter: " + country).setExact(true)
        )
    ).isVisible();
    return this;
  }

  private Locator countriesVisited(int count) {
    return page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName(countriesVisitedLabel(count)).setExact(true)
    );
  }

  private static String countriesVisitedLabel(int count) {
    if (count == 1) {
      return "1 country visited";
    }
    return count + " countries visited";
  }

  private void waitForFeed(Runnable click) {
    page.waitForResponse(
        response -> response.url().contains("/graphql")
            && response.request().postData() != null
            && response.request().postData().contains("GetFeed"),
        click
    );
  }
}
