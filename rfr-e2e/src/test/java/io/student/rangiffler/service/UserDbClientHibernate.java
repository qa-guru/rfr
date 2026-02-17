package io.student.rangiffler.service;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.*;
import io.student.rangiffler.data.repository.AuthUserRepository;
import io.student.rangiffler.data.repository.UserdataUserRepository;
import io.student.rangiffler.data.repository.impl.AuthUserRepositoryHibernate;
import io.student.rangiffler.data.repository.impl.UserdataUserRepositoryHibernate;
import io.student.rangiffler.data.tpl.XaTransactionTemplate;
import io.student.rangiffler.model.TestData;
import io.student.rangiffler.model.UserJson;
import io.qameta.allure.Step;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.*;

import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;
import static io.student.rangiffler.data.UserData.randomUsername;

@ParametersAreNonnullByDefault
public class UserDbClientHibernate {

    private static final Config CFG = Config.getInstance();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserRepository authUserRepository = new AuthUserRepositoryHibernate();
    private final UserdataUserRepository userdataUserRepository = new UserdataUserRepositoryHibernate();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(CFG.authJdbcUrl());
    private final XaTransactionTemplate xaTxApiTemplate = new XaTransactionTemplate(CFG.apiJdbcUrl());

    private final XaTransactionTemplate xaAuthApiTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.apiJdbcUrl()
    );

    @Step("Создать полного пользователя {username} (auth+userdata)")
    @Nonnull
    public UserJson createFullUser(String username, String password) {
        return xaAuthApiTemplate.execute(() -> {
            AuthUserEntity authUser = authUserEntity(username, password);
            authUserRepository.createUser(authUser);

            UserEntity ue = new UserEntity();
            ue.setUsername(username);

            CountryEntity country = new CountryEntity();
            country.setId(UUID.fromString("11f1070f-a2a0-6785-83d6-0242ac110002"));
            ue.setCountry(country);

            userdataUserRepository.create(ue);

            return buildUserJson(ue.getId().toString(), ue.getUsername());
        });
    }

    @Step("Найти пользователя по id {id}")
    @Nonnull
    public Optional<UserEntity> findById(UUID id) {
        return xaTxApiTemplate.execute(() ->
                userdataUserRepository.findById(id)
        );
    }

    @Step("Создать пользователя {userName} (Repository Hibernate)")
    @Nonnull
    public UserJson createUserRepositoryHibernate(String userName, String password) {
        AuthUserEntity newUser = xaTransactionTemplate.execute(() -> {
            {
                var authUserEntity = authUserEntity(userName, password);
                return authUserRepository.createUser(authUserEntity);
            }
        });
        return buildUserJson(String.valueOf(newUser.getId()),newUser.getUsername());
    }

    @Step("Создать пользователя в userdata")
    @Nonnull
    public UserEntity createUserdataUser(UserEntity user) {
        return xaTxApiTemplate.execute(() -> {
            userdataUserRepository.create(user);
            return user;
        });
    }

    @Step("Добавить исходящее приглашение в друзья")
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addOutcomeInvitation(requester, addressee);
            return null;
        });
    }

    @Step("Добавить входящее приглашение в друзья")
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addIncomeInvitation(requester, addressee);
            return null;
        });
    }

    @Step("Добавить пользователя в друзья")
    public void addFriend(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addFriend(requester, addressee);
            return null;
        });
    }

    @Nonnull
    private AuthUserEntity authUserEntity(String userName, String password) {
        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setUsername(userName);
        authUserEntity.setPassword(passwordEncoder.encode(password));
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
        return authUserEntity;
    }

    @Step("Добавить входящие приглашения ({count}) для пользователя {targetUser}")
    @Nonnull
    public List<UserJson> addIncomeInvitation(UserJson targetUser, int count) {
        if (count <= 0) return new ArrayList<>();

        return xaAuthApiTemplate.execute(() -> {
            final List<UserJson> result = new ArrayList<>(count);

            UserEntity targetEntity = userdataUserRepository.findByUsername(targetUser.data().user().username())
                    .orElseThrow();

            for (int i = 0; i < count; i++) {
                String username = randomUsername();

                AuthUserEntity authUser = authUserEntity(username, STANDART_PASSWORD);
                authUserRepository.createUser(authUser);

                UserEntity addressee = userdataUserRepository.create(userEntity(username));

                userdataUserRepository.addIncomeInvitation(targetEntity, addressee);

                result.add(buildUserJson(String.valueOf(addressee.getId()), addressee.getUsername()));
            }

            return result;
        });
    }

    @Step("Добавить исходящие приглашения ({count}) для пользователя {targetUser}")
    @Nonnull
    public List<UserJson> addOutcomeInvitation(UserJson targetUser, int count) {
        if (count <= 0) return new ArrayList<>();

        return xaAuthApiTemplate.execute(() -> {
            final List<UserJson> result = new ArrayList<>(count);

            UserEntity targetEntity = userdataUserRepository
                    .findByUsername(targetUser.data().user().username())
                    .orElseThrow();

            for (int i = 0; i < count; i++) {
                String username = randomUsername();

                AuthUserEntity authUser = authUserEntity(username, STANDART_PASSWORD);
                authUserRepository.createUser(authUser);

                UserEntity addressee = userdataUserRepository.create(userEntity(username));

                userdataUserRepository.addOutcomeInvitation(addressee, targetEntity);

                result.add(buildUserJson(addressee.getId().toString(), addressee.getUsername()));
            }

            return result;
        });
    }

    @Step("Добавить друзей ({count}) пользователю {targetUser}")
    @Nonnull
    public List<UserJson> addFriend(UserJson targetUser, int count) {
        if (count <= 0) return new ArrayList<>();

        return xaAuthApiTemplate.execute(() -> {
            final List<UserJson> result = new ArrayList<>(count);

            UserEntity targetEntity = userdataUserRepository
                    .findByUsername(targetUser.data().user().username())
                    .orElseThrow();

            for (int i = 0; i < count; i++) {
                String username = randomUsername();

                AuthUserEntity authUser = authUserEntity(username, STANDART_PASSWORD);
                authUserRepository.createUser(authUser);

                UserEntity addressee = userdataUserRepository.create(userEntity(username));

                userdataUserRepository.addFriend(targetEntity, addressee);

                result.add(buildUserJson(addressee.getId().toString(), addressee.getUsername()));
            }

            return result;
        });
    }

    @Step("Получить всех пользователей userdata")
    @Nonnull
    public List<UserEntity> findAll() {
        return xaTxApiTemplate.execute(userdataUserRepository::findAll);
    }

    @Nonnull
    private UserEntity userEntity(String username) {
        UserEntity ue = new UserEntity();
        ue.setUsername(username);

        CountryEntity country = new CountryEntity();
        country.setId(UUID.fromString("11f1070f-a2a0-6785-83d6-0242ac110002"));
        ue.setCountry(country);

        return ue;
    }

    @Nonnull
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
