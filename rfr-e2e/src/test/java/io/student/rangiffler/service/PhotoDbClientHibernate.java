package io.student.rangiffler.service;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.repository.PhotoRepository;
import io.student.rangiffler.data.repository.impl.PhotoRepositoryHibernate;
import io.student.rangiffler.data.tpl.XaTransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PhotoDbClientHibernate {

    private static final Config CFG = Config.getInstance();

    private final PhotoRepository photoRepository = new PhotoRepositoryHibernate();
    private final XaTransactionTemplate xaTxApiTemplate = new XaTransactionTemplate(CFG.apiJdbcUrl());

    public PhotoEntity createPhoto(PhotoEntity photo) {
        return xaTxApiTemplate.execute(() -> photoRepository.create(photo));
    }

    public PhotoEntity updatePhoto(PhotoEntity photo) {
        return xaTxApiTemplate.execute(() -> photoRepository.update(photo));
    }

    public Optional<PhotoEntity> findPhotoById(UUID id) {
        return xaTxApiTemplate.execute(() -> photoRepository.findById(id));
    }

    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        return xaTxApiTemplate.execute(() -> photoRepository.findByUsernameAndDescription(username, description));
    }

    public Optional<PhotoEntity> findByUsernameAndCountry(String username, String code) {
        return xaTxApiTemplate.execute(() -> photoRepository.findByUsernameAndCountry(username, code));
    }

    public List<PhotoEntity> findAllUserPhoto(String username) {
        return xaTxApiTemplate.execute(() -> photoRepository.findAllUserPhoto(username));
    }

    public void removePhoto(PhotoEntity photo) {
        xaTxApiTemplate.execute(() -> {
            photoRepository.remove(photo);
            return null;
        });
    }
}