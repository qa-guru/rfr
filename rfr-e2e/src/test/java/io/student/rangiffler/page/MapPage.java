package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class MapPage {

    private final SelenideElement map = $("figure.worldmap__figure-container");
    private final ElementsCollection sidebarItems = $$(".MuiListItemIcon-root");
    private final SelenideElement sidebarFriends = sidebarItems.get(2);

    public MapPage shouldBeVisibleMap() {
        map.shouldBe(visible);
        return this;
    }

    public MapPage shouldNotVisibleMap() {
        map.shouldNotBe(visible);
        return this;
    }

    public FriendsPage clickOnIconFriends() {
        sidebarFriends.click();
        return Selenide.page(FriendsPage.class);
    }
}
