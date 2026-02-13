package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.repository.impl.AuthUserRepositoryHibernate;
import io.student.rangiffler.data.repository.impl.AuthUserRepositoryJdbc;
import io.student.rangiffler.data.repository.impl.AuthUserRepositorySpringJdbc;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface AuthUserRepository {
    @Nonnull
    static AuthUserRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new AuthUserRepositoryJdbc();
            case "spring-jdbc" -> new AuthUserRepositorySpringJdbc();
            default -> new AuthUserRepositoryHibernate();
        };
    }
    @Nonnull
    AuthUserEntity createUser(AuthUserEntity user);
    @NotNull
    AuthUserEntity updateUser(AuthUserEntity user);
    void deleteUserByUserName(String userName);
    @Nonnull
    List<AuthUserEntity> findAll();
    @Nonnull
    Optional<AuthUserEntity> findById(UUID id);
    @Nonnull
    Optional<AuthUserEntity> findByUsername(String username);
}
