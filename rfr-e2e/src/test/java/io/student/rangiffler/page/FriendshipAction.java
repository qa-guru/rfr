package io.student.rangiffler.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FriendshipAction {
    ACCEPTED("Accept"),
    REJECTED("Decline"),
    ADD("Add"),
    WAITING("Waiting...");

    private final String uiText;
}