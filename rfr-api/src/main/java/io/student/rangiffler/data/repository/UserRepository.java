package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.projection.UserWithStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "coalesce(f1.status, f2.status), " +
          "case when f1.requester.username = :username then true " +
          "     when f2.addressee.username = :username then false " +
          "     else null end) " +
          "from UserEntity u " +
          "left join FriendshipEntity f1 on f1.requester = u and f1.addressee.username = :username " +
          "left join FriendshipEntity f2 on f2.addressee = u and f2.requester.username = :username " +
          "where u.username <> :username " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findAllUsersWithFriendshipStatus(@Param("username") String username,
                                                        Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "coalesce(f1.status, f2.status), " +
          "case when f1.requester.username = :username then true " +
          "     when f2.addressee.username = :username then false " +
          "     else null end) " +
          "from UserEntity u " +
          "left join FriendshipEntity f1 on f1.requester = u and f1.addressee.username = :username " +
          "left join FriendshipEntity f2 on f2.addressee = u and f2.requester.username = :username " +
          "where u.username <> :username " +
          "  and (lower(u.username) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.firstname) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.lastName) like lower(concat('%', :searchQuery, '%'))) " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findAllUsersWithFriendshipStatus(@Param("username") String username,
                                                        @Param("searchQuery") String searchQuery,
                                                        Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, true) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.addressee and f.requester.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.ACCEPTED " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findFriends(@Param("username") String username,
                                   Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, true) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.addressee and f.requester.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.ACCEPTED " +
          "  and (lower(u.username) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.firstname) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.lastName) like lower(concat('%', :searchQuery, '%'))) " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findFriends(@Param("username") String username,
                                   @Param("searchQuery") String searchQuery,
                                   Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, true) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.addressee and f.requester.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findOutcomeInvitations(@Param("username") String username,
                                              Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, true) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.addressee and f.requester.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING " +
          "  and (lower(u.username) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.firstname) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.lastName) like lower(concat('%', :searchQuery, '%'))) " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findOutcomeInvitations(@Param("username") String username,
                                              @Param("searchQuery") String searchQuery,
                                              Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, false) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.requester and f.addressee.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findIncomeInvitations(@Param("username") String username,
                                             Pageable pageable);

  @Query(
      "select new io.student.rangiffler.data.projection.UserWithStatus(" +
          "u.id, u.username, u.firstname, u.lastName, u.avatar, u.country.id, " +
          "f.status, false) " +
          "from UserEntity u " +
          "join FriendshipEntity f on u = f.requester and f.addressee.username = :username " +
          "where f.status = io.student.rangiffler.data.entity.FriendshipStatus.PENDING " +
          "  and (lower(u.username) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.firstname) like lower(concat('%', :searchQuery, '%')) " +
          "       or lower(u.lastName) like lower(concat('%', :searchQuery, '%'))) " +
          "order by u.username asc"
  )
  Page<UserWithStatus> findIncomeInvitations(@Param("username") String username,
                                             @Param("searchQuery") String searchQuery,
                                             Pageable pageable);
}
