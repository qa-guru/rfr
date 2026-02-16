package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.student.rangiffler.page.FriendshipAction.ADD;

public class FriendsPage {

    private final ElementsCollection friendsRows = $$(".MuiTableBody-root .MuiTableRow-root");
    private final SelenideElement peopleTabs = $("[aria-label='People tabs']");
    private final SelenideElement searchInput = $("[placeholder='Search people']");
    private final String friendsRowCells = ".MuiTableCell-root";

    private ElementsCollection getRowFriend(Integer indexRow){
        return friendsRows.get(indexRow).$$(friendsRowCells);
    }

    private SelenideElement rowByUsername(String username) {
        return friendsRows.findBy(text(username)).shouldBe(visible);
    }

    private SelenideElement rowByUsername(String username, WebElementCondition conditionVisible) {
        return friendsRows.findBy(text(username)).shouldBe(conditionVisible);
    }

    public FriendsPage shouldHaveUser(String username) {
        rowByUsername(username);
        return this;
    }

    public FriendsPage shouldHaveActionStatus(String username, FriendshipAction action) {
        rowByUsername(username).shouldHave(text(action.getUiText()));
        return this;
    }

    public FriendsPage shouldNotHaveUser(String username) {
        rowByUsername(username, hidden);
        return this;
    }

    public FriendsPage shouldHaveNoUsers() {
        $(".MuiTableBody-root").shouldBe(visible);
        friendsRows.shouldHave(size(0));
        return this;
    }

    public FriendsPage clickToOutcomeInvitations() {
        peopleTabs.$(byText("Outcome invitations")).click();
        return this;
    }

    public FriendsPage clickToIncomeInvitations() {
        peopleTabs.$(byText("Income invitations")).click();
        return this;
    }

    public FriendsPage clickToAllPeople() {
        peopleTabs.$(byText("All People")).click();
        return this;
    }

    public FriendsPage clickToFriends(){
        peopleTabs.$(byText("Friends")).click();
        return this;
    }

    public FriendsPage clickButtonByUserName(String username, FriendshipAction button) {
        rowByUsername(username).$(byText(button.getUiText())).click();
        return this;
    }

    public FriendsPage searchUser(String username) {
        searchInput.sendKeys(username);
        searchInput.pressEnter();
        return this;
    }
}
