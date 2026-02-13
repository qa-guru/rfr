package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.repository.AuthUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.student.rangiffler.data.jpa.EntityManagers.em;

public class AuthUserRepositoryHibernate implements AuthUserRepository {
    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.authJdbcUrl());

    @NotNull
    @Override
    public AuthUserEntity createUser(AuthUserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        return user;
    }

    @NotNull
    @Override
    public AuthUserEntity updateUser(AuthUserEntity user) {
        entityManager.joinTransaction();
        return entityManager.merge(user);
    }

    @NotNull
    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(AuthUserEntity.class, id));
    }

    @NotNull
    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery( "select U from AuthUserEntity where u.username = :username", AuthUserEntity.class)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }

    }

    @Override
    public void deleteUserByUserName(String userName) {
        throw new UnsupportedOperationException("Метод не поддерживается");

    }

    @NotNull
    @Override
    public List<AuthUserEntity> findAll() {
        return entityManager.createQuery(
                        "select u from AuthUserEntity u",
                        AuthUserEntity.class
                )
                .getResultList();
    }
}
