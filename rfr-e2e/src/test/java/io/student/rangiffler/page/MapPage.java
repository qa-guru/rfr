package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class MapPage {

    private final SelenideElement map = $("figure.worldmap__figure-container");
    private final ElementsCollection sidebarItems = $$(".MuiListItemIcon-root");
    private final SelenideElement sidebarFriends = sidebarItems.get(2);
    private final SelenideElement sidebarProfile = sidebarItems.get(0);

    @Step("Проверить что карта отображается")
    public MapPage shouldBeVisibleMap() {
        map.shouldBe(visible);
        return this;
    }

    @Step("Проверить что карта не отображается")
    public MapPage shouldNotVisibleMap() {
        map.shouldNotBe(visible);
        return this;
    }

    @Step("Перейти в раздел друзей")
    public FriendsPage clickOnIconFriends() {
        sidebarFriends.click();
        return Selenide.page(FriendsPage.class);
    }

    @Step("Перейти в профиль")
    public ProfilePage clickOnIconProfile() {
        sidebarProfile.click();
        return Selenide.page(ProfilePage.class);
    }
}
