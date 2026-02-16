package io.student.rangiffler.service;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.*;
import io.student.rangiffler.data.repository.AuthUserRepository;
import io.student.rangiffler.data.repository.UserdataUserRepository;
import io.student.rangiffler.data.repository.impl.AuthUserRepositoryJdbc;
import io.student.rangiffler.data.repository.impl.AuthUserRepositorySpringJdbc;
import io.student.rangiffler.data.repository.impl.UserdataUserRepositoryJdbc;
import io.student.rangiffler.data.tpl.JdbcTransactionTemplate;
import io.student.rangiffler.model.TestData;
import io.student.rangiffler.model.UserJson;
import io.qameta.allure.Step;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class UserDbClientRepository {

    private static final Config CFG = Config.getInstance();

    private final AuthUserRepository authUserRepository = new AuthUserRepositoryJdbc();
    private final AuthUserRepository authUserRepositorySpringJdbc = new AuthUserRepositorySpringJdbc();
    private final UserdataUserRepository userdataUserRepository = new UserdataUserRepositoryJdbc();

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(CFG.authJdbcUrl());
    private final JdbcTransactionTemplate jdbcTxApiTemplate = new JdbcTransactionTemplate(CFG.apiJdbcUrl());



    @Step("Получить список всех пользователей")
    public List<UserJson> findAllUsers() {
        return authUserRepository.findAll().stream()
                .map(x -> buildUserJson(x.getId().toString(), x.getUsername()))
                .toList();
    }

    @Step("Создать пользователя {userName} (Repository JDBC)")
    public UserJson createUserRepositoryJdbc(String userName, String password) {
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
                    authUserEntity.addAuthorities(
                            Arrays.stream(Authority.values())
                                    .map(a -> {
                                        AuthorityEntity ae = new AuthorityEntity();
                                        ae.setAuthority(a);
                                        return ae;
                                    })
                                    .toArray(AuthorityEntity[]::new)
                    );
                    AuthUserEntity user = authUserRepository.createUser(authUserEntity);

                    return user;
                }
        );
        return buildUserJson(newUser.getId().toString(), newUser.getUsername());
    }

    @Step("Добавить исходящее приглашение в друзья")
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        jdbcTxApiTemplate.execute(() -> {
            userdataUserRepository.addOutcomeInvitation(requester, addressee);
            return null;
        });
    }

    @Step("Добавить входящее приглашение в друзья")
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        jdbcTxApiTemplate.execute(() -> {
            userdataUserRepository.addIncomeInvitation(requester, addressee);
            return null;
        });
    }

    @Step("Добавить пользователя в друзья")
    public void addFriend(UserEntity requester, UserEntity addressee) {
        jdbcTxApiTemplate.execute(() -> {
            userdataUserRepository.addFriend(requester, addressee);
            return null;
        });
    }

    @Step("Найти пользователя по id {id}")
    public Optional<UserEntity> findById(UUID id) {
        return jdbcTxApiTemplate.execute(() -> userdataUserRepository.findById(id));
    }

    @Step("Получить пользователя по id {id}")
    public UserEntity getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found: id=" + id));
    }

    @Step("Создать пользователя в userdata")
    public UserEntity create(UserEntity user) {
        return jdbcTxApiTemplate.execute(() -> userdataUserRepository.create(user));
    }

    @Step("Получить список всех пользователей (Repository SpringJdbc)")
    public List<UserJson> findAllUsersSpringJdbc() {
        return authUserRepositorySpringJdbc.findAll().stream()
                .map(x -> buildUserJson(x.getId().toString(), x.getUsername()))
                .toList();
    }

    @Step("Создать пользователя {userName} (Repository SpringJdbc)")
    public UserJson createUserRepositorySpringJdbc(String userName, String password) {
        AuthUserEntity newUser = jdbcTxTemplate.execute(() -> {
            AuthUserEntity authUserEntity = new AuthUserEntity();
            authUserEntity.setId(UUID.randomUUID());
            authUserEntity.setUsername(userName);
            authUserEntity.setPassword(password);
            authUserEntity.setEnabled(true);
            authUserEntity.setAccountNonExpired(true);
            authUserEntity.setAccountNonLocked(true);
            authUserEntity.setCredentialsNonExpired(true);

            authUserEntity.addAuthorities(
                    Arrays.stream(Authority.values())
                            .map(a -> {
                                AuthorityEntity ae = new AuthorityEntity();
                                ae.setAuthority(a);
                                return ae;
                            })
                            .toArray(AuthorityEntity[]::new)
            );

            return authUserRepositorySpringJdbc.createUser(authUserEntity);
        });

        return buildUserJson(newUser.getId().toString(), newUser.getUsername());
    }

    @Step("Найти пользователя по username {username} (Repository SpringJdbc)")
    public Optional<UserJson> findUserJsonByUsernameSpringJdbc(String username) {
        return jdbcTxTemplate.execute(() ->
                authUserRepositorySpringJdbc.findByUsername(username)
                        .map(u -> buildUserJson(u.getId().toString(), u.getUsername()))
        );
    }

    @Step("Удалить пользователя по username {userName} (Repository SpringJdbc)")
    public void deleteUserByUsernameSpringJdbc(String userName) {
        jdbcTxTemplate.execute(() -> {
            authUserRepositorySpringJdbc.deleteUserByUserName(userName);
            return null;
        });
    }

    @Step("Найти пользователя по username {username} (Repository JDBC)")
    public Optional<UserJson> findUserJsonByUsernameJdbc(String username) {
        return jdbcTxTemplate.execute(() ->
                authUserRepository.findByUsername(username)
                        .map(u -> buildUserJson(u.getId().toString(), u.getUsername()))
        );
    }

    @Step("Удалить пользователя по username {userName} (Repository JDBC)")
    public void deleteUserByUsernameJdbc(String userName) {
        jdbcTxTemplate.execute(() -> {
            authUserRepository.deleteUserByUserName(userName);
            return null;
        });
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
