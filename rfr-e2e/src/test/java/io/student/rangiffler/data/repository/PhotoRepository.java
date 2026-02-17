package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.repository.impl.PhotoRepositoryHibernate;
import io.student.rangiffler.data.repository.impl.PhotoRepositoryJdbc;
import io.student.rangiffler.data.repository.impl.PhotoRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository {
    static PhotoRepository getInstance() {
        return switch (System.getProperty("repository.impl", "jpa")) {
            case "jdbc" -> new PhotoRepositoryJdbc();
            case "spring-jdbc" -> new PhotoRepositorySpringJdbc();
            default -> new PhotoRepositoryHibernate();
        };
    }
    PhotoEntity create(PhotoEntity photo);
    PhotoEntity update(PhotoEntity photo);
    Optional<PhotoEntity> findById(UUID id);
    Optional<PhotoEntity> findByUsernameAndDescription(String username, String description);
    Optional<PhotoEntity> findByUsernameAndCountry(String username, String code);
    List<PhotoEntity> findAllUserPhoto(String username);
    void remove(PhotoEntity photo);
}
