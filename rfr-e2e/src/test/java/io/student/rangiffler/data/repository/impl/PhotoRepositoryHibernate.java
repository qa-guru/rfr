package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.repository.PhotoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.student.rangiffler.data.jpa.EntityManagers.em;

public class PhotoRepositoryHibernate implements PhotoRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.apiJdbcUrl());

    @NotNull
    @Override
    public PhotoEntity create(PhotoEntity photo) {
        entityManager.joinTransaction();
        entityManager.persist(photo);
        return photo;
    }

    @NotNull
    @Override
    public PhotoEntity update(PhotoEntity photo) {
        entityManager.joinTransaction();
        return entityManager.merge(photo);
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(PhotoEntity.class, id));
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        try {
            return Optional.of(
                    entityManager.createQuery(
                                    "select p from PhotoEntity p " +
                                            "join p.user u " +
                                            "where u.username = :username and p.description = :description",
                                    PhotoEntity.class
                            )
                            .setParameter("username", username)
                            .setParameter("description", description)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndCountry(String username, String code) {
        try {
            return Optional.of(
                    entityManager.createQuery(
                                    "select p from PhotoEntity p " +
                                            "join p.user u " +
                                            "join p.country c " +
                                            "where u.username = :username and c.code = :code",
                                    PhotoEntity.class
                            )
                            .setParameter("username", username)
                            .setParameter("code", code)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Override
    public List<PhotoEntity> findAllUserPhoto(String username) {
        return entityManager.createQuery(
                        "select p from PhotoEntity p " +
                                "join p.user u " +
                                "where u.username = :username " +
                                "order by p.createdDate desc",
                        PhotoEntity.class
                )
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public void remove(PhotoEntity photo) {
        entityManager.joinTransaction();
        PhotoEntity managed = entityManager.contains(photo) ? photo : entityManager.merge(photo);
        entityManager.remove(managed);
    }
}