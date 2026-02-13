package io.student.rangiffler.model;


import java.util.ArrayList;
import java.util.List;

public record TestData(
        List<PhotoJson> photos
) {

    public TestData addPhotos(List<PhotoJson> newPhotos) {
        List<PhotoJson> all = new ArrayList<>(photos == null ? List.of() : photos);
        all.addAll(newPhotos);
        return new TestData(all);
    }
}
