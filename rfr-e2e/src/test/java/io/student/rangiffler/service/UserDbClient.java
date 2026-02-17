package io.student.rangiffler.service;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.dao.AuthUserDao;
import io.student.rangiffler.data.dao.AuthorityDao;
import io.student.rangiffler.data.dao.impl.AuthUserDaoJdbc;
import io.student.rangiffler.data.dao.impl.AuthUserDaoSpringJdbc;
import io.student.rangiffler.data.dao.impl.AuthorityDaoJdbc;
import io.student.rangiffler.data.dao.impl.AuthorityDaoSpringJdbc;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.Authority;
import io.student.rangiffler.data.entity.AuthorityEntity;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.tpl.JdbcTransactionTemplate;
import io.student.rangiffler.data.tpl.XaTransactionTemplate;
import io.student.rangiffler.model.TestData;
import io.student.rangiffler.model.UserJson;
import io.qameta.allure.Step;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class UserDbClient {

    private static final Config CFG = Config.getInstance();

    private final AuthUserDao userDao = new AuthUserDaoJdbc();
    private final AuthorityDao authorityDao = new AuthorityDaoJdbc();

    private final AuthUserDao userDaoSpringJdbc = new AuthUserDaoSpringJdbc();
    private final AuthorityDao authorityDaoSpringJdbc = new AuthorityDaoSpringJdbc();

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(CFG.authJdbcUrl());
    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(CFG.authJdbcUrl());


    @Step("Удалить пользователя по username {username}")
    public void deleteUserByUsername(String username) {
        xaTransactionTemplate.execute(() -> {
            authorityDao.deleteAuthorityByUserName(username);
            userDao.deleteUserByUserName(username);
            return null;
        });
    }

    @Step("Получить список всех пользователей")
    public List<UserJson> findAllUsers() {
        return userDaoSpringJdbc.findAll().stream()
                .map(x -> buildUserJson(x.getId().toString(), x.getUsername()))
                .toList();
    }

    @Step("Получить список всех пользователей (SpringJdbc)")
    public List<UserJson> findAllUsersSpringJdbc() {
        return userDao.findAll().stream()
                .map(x -> buildUserJson(x.getId().toString(), x.getUsername()))
                .toList();
    }

    @Step("Получить все роли/права")
    public List<AuthorityEntity> findAllAuthority() {
        return authorityDao.findAll();
    }

    @Step("Получить все роли/права (SpringJdbc)")
    public List<AuthorityEntity> findAllAuthoritySpringJdbc() {
        return authorityDaoSpringJdbc.findAll();
    }

    @Step("Создать пользователя {userName}")
    public UserJson createUser(String userName, String password) {
        AuthUserEntity newUser = xaTransactionTemplate.execute(
                        ()-> {
                            AuthUserEntity authUserEntity = new AuthUserEntity();
                            authUserEntity.setId(UUID.randomUUID());
                            authUserEntity.setUsername(userName);
                            authUserEntity.setPassword(password);
                            authUserEntity.setEnabled(true);
                            authUserEntity.setAccountNonExpired(true);
                            authUserEntity.setAccountNonLocked(true);
                            authUserEntity.setCredentialsNonExpired(true);
                            AuthUserEntity user = userDao.createUser(authUserEntity);

                            authorityDao.createAuthority(
                                    Arrays.stream(Authority.values())
                                            .map(a -> {
                                                        AuthorityEntity ae = new AuthorityEntity();
                                                        ae.setUser(user);
                                                        ae.setAuthority(a);
                                                        return ae;
                                                    }
                                            )
                                            .toArray(AuthorityEntity[]::new));
                            return user;
                        }
                );
        return buildUserJson(newUser.getId().toString(), newUser.getUsername());
    }

    @Step("Удалить пользователя по username {username} (SpringJdbc)")
    public void deleteUserByUsernameSpringJdbc(String username) {
        jdbcTxTemplate.execute(() -> {
            authorityDaoSpringJdbc.deleteAuthorityByUserName(username);
            userDaoSpringJdbc.deleteUserByUserName(username);
            return null;
        });
    }

    @Step("Создать пользователя {userName} (SpringJdbc)")
    public UserJson createUserSpringJdbc(String userName, String password) {
        AuthUserEntity newUser = jdbcTxTemplate.execute(
                ()-> {
                    AuthUserEntity authUserEntity = new AuthUserEntity();
                    authUserEntity.setId(UUID.randomUUID());
                    authUserEntity.setUsername(userName);
                    authUserEntity.setPassword(password);
                    authUserEntity.setEnabled(true);
                    authUserEntity.setAccountNonExpired(true);
                    authUserEntity.setAccountNonLocked(true);
                    authUserEntity.setCredentialsNonExpired(true);
                    AuthUserEntity user = userDaoSpringJdbc.createUser(authUserEntity);

                    authorityDaoSpringJdbc.createAuthority(
                            Arrays.stream(Authority.values())
                                    .map(a -> {
                                                AuthorityEntity ae = new AuthorityEntity();
                                                ae.setUser(user);
                                                ae.setAuthority(a);
                                                return ae;
                                            }
                                    )
                                    .toArray(AuthorityEntity[]::new));
                    return user;
                }
        );
        return buildUserJson(newUser.getId().toString(), newUser.getUsername());
    }

    private UserJson buildUserJson(String userId, String userName) {
        return new UserJson(
                new UserJson.Data(
                        new UserJson.User(
                                userId,
                                userName,
                                null,
                                null,
                                null,
                                null
                        )
                ), null
        );
    }
}
