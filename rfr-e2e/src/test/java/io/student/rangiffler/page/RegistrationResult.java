package io.student.rangiffler.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationResult {
    SUCCESS("Congratulations! You've registered!"),
    PASSWORD_NOT_EQUAL("Passwords should be equal");

    private final String text;
}
