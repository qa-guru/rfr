package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.repository.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface UserdataUserRepository {
    @Nonnull
    static UserdataUserRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new UserdataUserRepositoryJdbc();
            case "spring-jdbc" -> new UserdataUserRepositorySpringJdbc();
            default -> new UserdataUserRepositoryHibernate();
        };
    }
    @Nonnull
    UserEntity create(UserEntity user);
    UserEntity update(UserEntity user);
    @Nonnull
    Optional<UserEntity> findById(UUID id);
    void addIncomeInvitation(UserEntity requester, UserEntity addressee);
    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);
    void addFriend(UserEntity requester, UserEntity addressee);
    @Nonnull
    Optional<UserEntity> findByUsername(String username);
    @Nonnull
    List<UserEntity> findAll();
}
