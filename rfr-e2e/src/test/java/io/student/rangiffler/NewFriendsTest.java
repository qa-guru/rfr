package io.student.rangiffler;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.extension.UserExtension;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.page.AuthChoicePage;
import io.student.rangiffler.page.FriendshipAction;
import io.student.rangiffler.service.UserDbClientHibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;

@ExtendWith(UserExtension.class)
public class NewFriendsTest {

    private static final Config CFG = Config.getInstance();
    private final UserDbClientHibernate userDbClientHibernate = new UserDbClientHibernate();


    @AfterEach
    public void afterEach() {
        clearBrowserLocalStorage();
        clearBrowserCookies();
        closeWebDriver();

    }

    @Test
    @User(incomeInvitations = 1)
    void addingNewFriend(UserJson user) {
        String usernameIncome = user.testData().incomeInvitations().get(0).data().user().username();
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.data().user().username(), STANDART_PASSWORD)
                .shouldBeVisibleMap()
                .clickOnIconFriends()
                .clickToIncomeInvitations()
                .shouldHaveUser(usernameIncome)
                .clickButtonByUserName(user.testData().incomeInvitations().get(0).data().user().username(),
                        FriendshipAction.ACCEPTED)
                .clickToFriends()
                .shouldHaveUser(usernameIncome);
    }

    @Test
    @User(incomeInvitations = 1)
    void rejectNewFriend(UserJson user) {
        String usernameIncome = user.testData().incomeInvitations().get(0).data().user().username();
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.data().user().username(), STANDART_PASSWORD)
                .shouldBeVisibleMap()
                .clickOnIconFriends()
                .clickToIncomeInvitations()
                .shouldHaveUser(usernameIncome)
                .clickButtonByUserName(user.testData().incomeInvitations().get(0).data().user().username(),
                        FriendshipAction.REJECTED)
                .clickToFriends()
                .shouldNotHaveUser(usernameIncome);
    }

    @Test
    @User
    void friendIncome(UserJson user) {
        List<UserEntity> userEntities = userDbClientHibernate.findAll();
        String username = userEntities.getFirst().getUsername();
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.data().user().username(), STANDART_PASSWORD)
                .shouldBeVisibleMap()
                .clickOnIconFriends()
                .clickToAllPeople()
                .searchUser(username)
                .clickButtonByUserName(username, FriendshipAction.ADD)
                .clickToOutcomeInvitations()
                .shouldHaveActionStatus(username, FriendshipAction.WAITING);
    }

    @Test
    @User
    void changeProfile(UserJson user) {
        String firstName = "firstName";
        String sureName = "sureName";
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.data().user().username(), STANDART_PASSWORD)
                .shouldBeVisibleMap()
                .clickOnIconProfile()
                .changeFirstNameAndSureName(firstName, sureName)
                .reloadPage()
                .shouldHaveFullName(firstName, sureName);

    }
}
