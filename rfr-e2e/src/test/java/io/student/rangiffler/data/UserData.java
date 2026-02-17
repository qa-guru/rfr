package io.student.rangiffler.data;

import net.datafaker.Faker;

public class UserData {
    private static final Faker faker = new Faker();
    public static final String STANDART_PASSWORD = "12345";

    public static final String randomUsername() {
        return faker.internet().username();
    }
}
