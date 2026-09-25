package io.student.rangiffler.service.impl;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.FriendshipEntity;
import io.student.rangiffler.data.entity.FriendshipStatus;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.projection.UserWithStatus;
import io.student.rangiffler.data.repository.CountryRepository;
import io.student.rangiffler.data.repository.UserRepository;
import io.student.rangiffler.exception.FriendshipActionException;
import io.student.rangiffler.exception.ResourceNotFoundException;
import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.model.types.FriendStatus;
import io.student.rangiffler.model.types.Stat;
import io.student.rangiffler.model.types.User;
import io.student.rangiffler.model.types.UserInput;
import io.student.rangiffler.service.api.UserService;
import io.student.rangiffler.util.BytesAsString;
import io.student.rangiffler.util.StringAsBytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final CountryRepository countryRepository;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         CountryRepository countryRepository) {
    this.userRepository = userRepository;
    this.countryRepository = countryRepository;
  }

  @Override
  @Transactional
  public User createNewUserIfNotPresent(String username) {
    UserEntity userEntity = userRepository.findByUsername(username)
        .orElseGet(() -> {
          UserEntity newUser = new UserEntity();
          newUser.setUsername(username);
          newUser.setCountry(countryRepository.findByCode("ru").orElseThrow(() -> new ResourceNotFoundException(
              "Country not found by code: ru"
          )));
          return userRepository.save(newUser);
        });
    return toUser(userEntity, null);
  }

  @Override
  @Transactional(readOnly = true)
  public User currentUser(String username) {
    UserEntity userEntity = getRequiredUser(username);
    return toUser(userEntity, null);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> allUsers(String username, Pageable pageable, String searchQuery) {
    return (searchQuery != null && !searchQuery.isBlank())
        ? userRepository.findAllUsersWithFriendshipStatus(username, searchQuery, pageable)
        .map(this::toUserFromProjection)
        : userRepository.findAllUsersWithFriendshipStatus(username, pageable)
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> friends(String username, Pageable pageable, String searchQuery) {
    return (searchQuery != null && !searchQuery.isBlank())
        ? userRepository.findFriends(username, searchQuery, pageable)
        .map(this::toUserFromProjection)
        : userRepository.findFriends(username, pageable)
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> incomeInvitations(String username, Pageable pageable, String searchQuery) {
    return (searchQuery != null && !searchQuery.isBlank())
        ? userRepository.findIncomeInvitations(username, searchQuery, pageable)
        .map(this::toUserFromProjection)
        : userRepository.findIncomeInvitations(username, pageable)
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> outcomeInvitations(String username, Pageable pageable, String searchQuery) {
    return (searchQuery != null && !searchQuery.isBlank())
        ? userRepository.findOutcomeInvitations(username, searchQuery, pageable)
        .map(this::toUserFromProjection)
        : userRepository.findOutcomeInvitations(username, pageable)
        .map(this::toUserFromProjection);
  }

  @Override
  @Transactional
  public User updateUser(String username, UserInput input) {
    UserEntity userEntity = getRequiredUser(username);

    if (input.getFirstname() != null) {
      userEntity.setFirstname(input.getFirstname());
    }
    if (input.getSurname() != null) {
      userEntity.setLastName(input.getSurname());
    }
    if (input.getAvatar() != null) {
      userEntity.setAvatar(new StringAsBytes(input.getAvatar()).bytes());
    }
    if (input.getLocation() != null) {
      CountryEntity country = countryRepository.findByCode(input.getLocation().getCode())
          .orElseThrow(() -> new ResourceNotFoundException(
              String.format("Country not found by code: %s", input.getLocation().getCode())));
      userEntity.setCountry(country);
    }

    UserEntity saved = userRepository.save(userEntity);
    return toUser(saved, null);
  }

  @Override
  @Transactional
  public User addFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getTargetUser(currentUser, friendId);

    Optional<FriendshipEntity> outgoing = outgoingFriendship(currentUser, friend);
    Optional<FriendshipEntity> incoming = incomingFriendship(currentUser, friend);
    if (Stream.of(outgoing, incoming).flatMap(Optional::stream)
        .anyMatch(fe -> fe.getStatus() == FriendshipStatus.ACCEPTED)) {
      throw new FriendshipActionException("Already friends");
    }
    if (outgoing.isPresent()) {
      throw new FriendshipActionException("Invitation already sent");
    }
    if (incoming.isPresent()) {
      throw new FriendshipActionException("There is already an incoming invitation from this user");
    }

    currentUser.addFriends(FriendshipStatus.PENDING, friend);

    return toUser(friend, FriendStatus.INVITATION_SENT);
  }

  @Override
  @Transactional
  public User acceptInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity inviteUser = getTargetUser(currentUser, friendId);

    FriendshipEntity invite = requirePendingInvitation(currentUser, inviteUser);

    invite.setStatus(FriendshipStatus.ACCEPTED);
    currentUser.addFriends(FriendshipStatus.ACCEPTED, inviteUser);

    return toUser(inviteUser, FriendStatus.FRIEND);
  }

  @Override
  @Transactional
  public User declineInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friendToDecline = getTargetUser(currentUser, friendId);

    requirePendingInvitation(currentUser, friendToDecline);
    currentUser.removeInvites(friendToDecline);
    return toUser(friendToDecline, FriendStatus.NOT_FRIEND);
  }

  @Override
  @Transactional
  public User removeFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getTargetUser(currentUser, friendId);

    if (outgoingFriendship(currentUser, friend).isEmpty() && incomingFriendship(currentUser, friend).isEmpty()) {
      throw new FriendshipActionException("No friendship or invitation with this user");
    }
    currentUser.removeFriends(friend);
    currentUser.removeInvites(friend);
    return toUser(friend, FriendStatus.NOT_FRIEND);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Stat> stat(String username, boolean withFriends) {
    return List.of();
  }

  private User toUser(UserEntity entity, FriendStatus friendStatus) {
    return User.newBuilder()
        .id(entity.getId().toString())
        .username(entity.getUsername())
        .firstname(entity.getFirstname())
        .surname(entity.getLastName())
        .avatar(new BytesAsString(entity.getAvatar()).string())
        .friendStatus(friendStatus)
        .location(entity.getCountry() != null ? toCountry(entity.getCountry()) : null)
        .build();
  }

  private Country toCountry(CountryEntity entity) {
    return Country.newBuilder()
        .code(entity.getCode())
        .name(entity.getName())
        .flag(new BytesAsString(entity.getFlag()).string())
        .build();
  }

  private User toUserFromProjection(UserWithStatus projection) {
    FriendStatus friendStatus = calculateFriendStatus(
        projection.friendshipStatus(),
        projection.isRequester()
    );

    return User.newBuilder()
        .id(projection.id().toString())
        .username(projection.username())
        .firstname(projection.firstname())
        .surname(projection.lastName())
        .avatar(new BytesAsString(projection.avatar()).string())
        .friendStatus(friendStatus)
        .location(Country.newBuilder()
            .code(projection.countryCode())
            .name(projection.countryName())
            .flag(new BytesAsString(projection.countryFlag()).string())
            .build())
        .build();
  }

  private FriendStatus calculateFriendStatus(FriendshipStatus status, Boolean isRequester) {
    if (status == FriendshipStatus.ACCEPTED) {
      return FriendStatus.FRIEND;
    }
    if (status == FriendshipStatus.PENDING) {
      return Boolean.TRUE.equals(isRequester)
          ? FriendStatus.INVITATION_SENT
          : FriendStatus.INVITATION_RECEIVED;
    }
    return FriendStatus.NOT_FRIEND;
  }

  private UserEntity getRequiredUser(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("User not found by username: %s", username)
        ));
  }

  private UserEntity getTargetUser(UserEntity currentUser, UUID targetId) {
    if (currentUser.getId().equals(targetId)) {
      throw new FriendshipActionException("Cannot perform friendship action on yourself");
    }
    return getRequiredUser(targetId);
  }

  private Optional<FriendshipEntity> outgoingFriendship(UserEntity currentUser, UserEntity target) {
    return currentUser.getFriendshipRequests().stream()
        .filter(fe -> fe.getAddressee().getId().equals(target.getId()))
        .findFirst();
  }

  private Optional<FriendshipEntity> incomingFriendship(UserEntity currentUser, UserEntity target) {
    return currentUser.getFriendshipAddressees().stream()
        .filter(fe -> fe.getRequester().getId().equals(target.getId()))
        .findFirst();
  }

  private FriendshipEntity requirePendingInvitation(UserEntity currentUser, UserEntity requester) {
    return incomingFriendship(currentUser, requester)
        .filter(fe -> fe.getStatus() == FriendshipStatus.PENDING)
        .orElseThrow(() -> new FriendshipActionException("No pending invitation from this user"));
  }

  private UserEntity getRequiredUser(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("User not found by id: %s", userId)
        ));
  }
}
