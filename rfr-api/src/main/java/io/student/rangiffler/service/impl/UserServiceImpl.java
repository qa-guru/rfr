package io.student.rangiffler.service.impl;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.FriendshipEntity;
import io.student.rangiffler.data.entity.FriendshipStatus;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.projection.UserWithStatus;
import io.student.rangiffler.data.repository.CountryRepository;
import io.student.rangiffler.data.repository.UserRepository;
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
import java.util.UUID;

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
              "Страна не найден по коду: ru"
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
              String.format("Страна не найдена по коду: %s", input.getLocation().getCode())));
      userEntity.setCountry(country);
    }

    UserEntity saved = userRepository.save(userEntity);
    return toUser(saved, null);
  }

  @Override
  @Transactional
  public User addFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getRequiredUser(friendId);

    currentUser.addFriends(FriendshipStatus.PENDING, friend);
    userRepository.save(currentUser);

    return toUser(friend, FriendStatus.INVITATION_SENT);
  }

  @Override
  @Transactional
  public User acceptInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity inviteUser = getRequiredUser(friendId);

    FriendshipEntity invite = currentUser.getFriendshipAddressees()
        .stream()
        .filter(fe -> fe.getRequester().getUsername().equals(inviteUser.getUsername()))
        .findFirst()
        .orElseThrow();

    invite.setStatus(FriendshipStatus.ACCEPTED);
    currentUser.addFriends(FriendshipStatus.ACCEPTED, inviteUser);
    userRepository.save(currentUser);

    return toUser(inviteUser, FriendStatus.FRIEND);
  }

  @Override
  @Transactional
  public User declineInvitation(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friendToDecline = getRequiredUser(friendId);

    currentUser.removeInvites(friendToDecline);
    friendToDecline.removeFriends(currentUser);

    userRepository.save(currentUser);
    userRepository.save(friendToDecline);
    return toUser(friendToDecline, FriendStatus.NOT_FRIEND);
  }

  @Override
  @Transactional
  public User removeFriend(String username, UUID friendId) {
    UserEntity currentUser = getRequiredUser(username);
    UserEntity friend = getRequiredUser(friendId);

    currentUser.removeFriends(friend);
    currentUser.removeInvites(friend);
    friend.removeFriends(currentUser);
    friend.removeInvites(currentUser);
    userRepository.save(currentUser);
    userRepository.save(friend);
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

    Country country = null;
    if (projection.countryId() != null) {
      CountryEntity countryEntity = countryRepository.findById(projection.countryId()).orElse(null);
      if (countryEntity != null) {
        country = toCountry(countryEntity);
      }
    }

    return User.newBuilder()
        .id(projection.id().toString())
        .username(projection.username())
        .firstname(projection.firstname())
        .surname(projection.lastName())
        .avatar(new BytesAsString(projection.avatar()).string())
        .friendStatus(friendStatus)
        .location(country)
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
            String.format("Пользователь не найден по username: %s", username)
        ));
  }

  private UserEntity getRequiredUser(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("Пользователь не найден по id: %s", userId)
        ));
  }
}
