package io.student.rangiffler.data.projection;

import io.student.rangiffler.data.entity.FriendshipStatus;

import java.util.UUID;

public record UserWithStatus(
    UUID id,
    String username,
    String firstname,
    String lastName,
    byte[] avatar,
    UUID countryId,
    FriendshipStatus friendshipStatus,
    Boolean isRequester
) {
}
