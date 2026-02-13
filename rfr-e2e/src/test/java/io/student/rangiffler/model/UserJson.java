package io.student.rangiffler.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

public record UserJson(
        Data data,
        @JsonIgnore
        TestData testData
) {

    public UserJson addTestData(TestData testData) {
        return new UserJson(
                this.data,
                testData
        );
    }

    public record Data(
            User user
    ) {}

    public record User(
            String id,
            String username,
            String firstname,
            String surname,
            String avatar,
            Location location
    ) {}

    public record Location(
            String code,
            String name
    ) {}
}
