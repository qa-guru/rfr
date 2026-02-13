package io.student.rangiffler;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.jupiter.extension.UsersQueueExtension;
import io.student.rangiffler.page.AuthChoicePage;
import io.student.rangiffler.service.UserDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static io.student.rangiffler.jupiter.extension.UsersQueueExtension.StaticUser;
import static io.student.rangiffler.jupiter.extension.UsersQueueExtension.UserType;
import static io.student.rangiffler.jupiter.extension.UsersQueueExtension.UserType.Type.*;


public class FriendsWebTest {

    private static final Config CFG = Config.getInstance();
    private static final UserDbClient userDbClient = new UserDbClient();

    @AfterEach
    public void afterEach() {
        clearBrowserLocalStorage();
        clearBrowserCookies();
        closeWebDriver();

    }

    @Test
    @ExtendWith(UsersQueueExtension.class)
    void friendShouldBePresentInFriendsTable(@UserType(WITH_FRIEND) StaticUser user) {
        userDbClient.deleteUserByUsername(user.username());
        userDbClient.createUser(user.username(), user.password());
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.username(), user.password())
                .shouldBeVisibleMap()
                .clickOnIconFriends()
                .shouldHaveUser(user.friend());
    }
    @Test
    @ExtendWith(UsersQueueExtension.class)
    void friendsTableShouldBeEmptyForNewUser(@UserType(EMPTY) StaticUser user) {
        userDbClient.deleteUserByUsername(user.username());
        userDbClient.createUser(user.username(), user.password());
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.username(), user.password())
                .clickOnIconFriends()
                .shouldHaveNoUsers();
    }
    @Test
    @ExtendWith(UsersQueueExtension.class)
    void incomeInvitationBePresentInFriendsTable(@UserType(WITH_INCOME_REQUEST) StaticUser user) {
        userDbClient.deleteUserByUsername(user.username());
        userDbClient.createUser(user.username(), user.password());
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.username(), user.password())
                .clickOnIconFriends()
                .clickToIncomeInvitations()
                .shouldHaveUser(user.income());
    }
    @Test
    @ExtendWith(UsersQueueExtension.class)
    void outcomeInvitationBePresentInAllPeoplesTable(@UserType(WITH_OUTCOME_REQUEST) StaticUser user) {
        userDbClient.deleteUserByUsername(user.username());
        userDbClient.createUser(user.username(), user.password());
        open(CFG.frontUrl(), AuthChoicePage.class)
                .clickLogin()
                .login(user.username(), user.password())
                .clickOnIconFriends()
                .clickToOutcomeInvitations()
                .shouldHaveUser(user.outcome());
    }
}
