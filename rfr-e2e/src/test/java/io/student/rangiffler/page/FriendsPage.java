package io.student.rangiffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class FriendsPage {

    private final ElementsCollection friendsRows = $$(".MuiTableBody-root .MuiTableRow-root");
    private final SelenideElement peopleTabs = $("[aria-label='People tabs']");
    private final String friendsRowCells = ".MuiTableCell-root";


    private ElementsCollection getRowFriend(Integer indexRow){
        return friendsRows.get(indexRow).$$(friendsRowCells);
    }

    private SelenideElement rowByUsername(String username) {
        return friendsRows.findBy(text(username)).shouldBe(visible);
    }

    public FriendsPage shouldHaveUser(String username) {
        rowByUsername(username);
        return this;
    }

    public FriendsPage shouldHaveNoUsers() {
        $(".MuiTableBody-root").shouldBe(visible);
        friendsRows.shouldHave(size(0));
        return this;
    }

    public FriendsPage clickToOutcomeInvitations(){
        peopleTabs.$(byText("Outcome invitations")).click();
        return this;
    }

    public FriendsPage clickToIncomeInvitations(){
        peopleTabs.$(byText("Income invitations")).click();
        return this;
    }


}
