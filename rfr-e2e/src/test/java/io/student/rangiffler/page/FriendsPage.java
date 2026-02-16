package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.qameta.allure.Step;

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

    @Step("Проверить наличие пользователя {username} в списке")
    public FriendsPage shouldHaveUser(String username) {
        rowByUsername(username);
        return this;
    }

    @Step("Проверить статус действия {action} для пользователя {username}")
    public FriendsPage shouldHaveActionStatus(String username, FriendshipAction action) {
        rowByUsername(username).shouldHave(text(action.getUiText()));
        return this;
    }

    @Step("Проверить отсутствие пользователя {username} в списке")
    public FriendsPage shouldNotHaveUser(String username) {
        rowByUsername(username, hidden);
        return this;
    }

    @Step("Проверить что список пользователей пуст")
    public FriendsPage shouldHaveNoUsers() {
        $(".MuiTableBody-root").shouldBe(visible);
        friendsRows.shouldHave(size(0));
        return this;
    }

    @Step("Открыть исходящие приглашения")
    public FriendsPage clickToOutcomeInvitations() {
        peopleTabs.$(byText("Outcome invitations")).click();
        return this;
    }

    @Step("Открыть входящие приглашения")
    public FriendsPage clickToIncomeInvitations() {
        peopleTabs.$(byText("Income invitations")).click();
        return this;
    }

    @Step("Открыть вкладку Все")
    public FriendsPage clickToAllPeople() {
        peopleTabs.$(byText("All People")).click();
        return this;
    }

    @Step("Открыть вкладку Друзья")
    public FriendsPage clickToFriends(){
        peopleTabs.$(byText("Friends")).click();
        return this;
    }

    @Step("Нажать кнопку {button} для пользователя {username}")
    public FriendsPage clickButtonByUserName(String username, FriendshipAction button) {
        rowByUsername(username).$(byText(button.getUiText())).click();
        return this;
    }

    @Step("Поиск пользователя {username}")
    public FriendsPage searchUser(String username) {
        searchInput.sendKeys(username);
        searchInput.pressEnter();
        return this;
    }
}
