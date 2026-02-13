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
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<UserEntity> findById(UUID id) {
        return xaTxApiTemplate.execute(() ->
                userdataUserRepository.findById(id)
        );
    }

    public UserJson createUserRepositoryHibernate(String userName, String password) {
        AuthUserEntity newUser = xaTransactionTemplate.execute(() -> {
            {
                var authUserEntity = authUserEntity(userName, password);
                return authUserRepository.createUser(authUserEntity);
            }
        });
        return buildUserJson(String.valueOf(newUser.getId()),newUser.getUsername());
    }

    public UserEntity createUserdataUser(UserEntity user) {
        return xaTxApiTemplate.execute(() -> {
            userdataUserRepository.create(user);
            return user;
        });
    }

    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addOutcomeInvitation(requester, addressee);
            return null;
        });
    }

    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addIncomeInvitation(requester, addressee);
            return null;
        });
    }

    public void addFriend(UserEntity requester, UserEntity addressee) {
        xaTxApiTemplate.execute(() -> {
            userdataUserRepository.addFriend(requester, addressee);
            return null;
        });
    }

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
