package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.FriendShipId;
import io.student.rangiffler.data.entity.FriendshipEntity;
import io.student.rangiffler.data.entity.FriendshipStatus;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.repository.UserdataUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.student.rangiffler.data.jpa.EntityManagers.em;

public class UserdataUserRepositoryHibernate implements UserdataUserRepository {
    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.apiJdbcUrl());


    @NotNull
    @Override
    public UserEntity create(UserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        return user;
    }

    @Override
    public UserEntity update(UserEntity user) {
        entityManager.joinTransaction();
        return entityManager.merge(user);
    }

    @NotNull
    @Override
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }


    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        upsertFriendship(requester.getId(), addressee.getId(), FriendshipStatus.PENDING);

    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        upsertFriendship(addressee.getId(), requester.getId(), FriendshipStatus.PENDING);
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        upsertFriendship(requester.getId(), addressee.getId(), FriendshipStatus.ACCEPTED);
        upsertFriendship(addressee.getId(), requester.getId(), FriendshipStatus.ACCEPTED);
    }

    @NotNull
    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery( "select U from UserEntity where u.username = :username", UserEntity.class)
                    .setParameter("username", username).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Override
    public List<UserEntity> findAll() {
        return entityManager.createQuery("select u from UserEntity u", UserEntity.class)
                .getResultList();
    }

    private FriendShipId fid(UUID requesterId, UUID addresseeId) {
        return FriendShipId.builder()
                .requester(requesterId)
                .addressee(addresseeId)
                .build();
    }

    private void upsertFriendship(UUID requesterId, UUID addresseeId, FriendshipStatus status) {
        FriendShipId id = new FriendShipId(requesterId, addresseeId);

        FriendshipEntity f = entityManager.find(FriendshipEntity.class, id);

        if (f == null) {
            UserEntity requester = entityManager.getReference(UserEntity.class, requesterId);
            UserEntity addressee = entityManager.getReference(UserEntity.class, addresseeId);

            f = FriendshipEntity.builder()
                    .requester(requester)
                    .addressee(addressee)
                    .createdDate(new Date())
                    .status(status)
                    .build();

            entityManager.persist(f);
        } else {
            f.setStatus(status);
        }
    }
}
