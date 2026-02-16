package io.student.rangiffler.model;


import java.util.ArrayList;
import java.util.List;

public record TestData(
        String password,
        List<UserJson> incomeInvitations,
        List<UserJson> outcomeInvitations,
        List<UserJson> friends,
        List<PhotoJson> photos
) {

    public TestData(List<PhotoJson> photos) {
        this(null, List.of(), List.of(), List.of(), photos);
    }

    public TestData addPhotos(List<PhotoJson> newPhotos) {
        List<PhotoJson> all = new ArrayList<>(photos == null ? List.of() : photos);
        all.addAll(newPhotos);
        return new TestData(password, incomeInvitations, outcomeInvitations, friends, all);
    }
}