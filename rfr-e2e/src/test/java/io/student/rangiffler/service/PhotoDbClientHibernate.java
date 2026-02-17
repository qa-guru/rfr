package io.student.rangiffler.service;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.repository.PhotoRepository;
import io.student.rangiffler.data.repository.impl.PhotoRepositoryHibernate;
import io.student.rangiffler.data.tpl.XaTransactionTemplate;
import io.qameta.allure.Step;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PhotoDbClientHibernate {

    private static final Config CFG = Config.getInstance();

    private final PhotoRepository photoRepository = new PhotoRepositoryHibernate();
    private final XaTransactionTemplate xaTxApiTemplate = new XaTransactionTemplate(CFG.apiJdbcUrl());

    @Step("Создать фото")
    public PhotoEntity createPhoto(PhotoEntity photo) {
        return xaTxApiTemplate.execute(() -> photoRepository.create(photo));
    }

    @Step("Обновить фото")
    public PhotoEntity updatePhoto(PhotoEntity photo) {
        return xaTxApiTemplate.execute(() -> photoRepository.update(photo));
    }

    @Step("Найти фото по id {id}")
    public Optional<PhotoEntity> findPhotoById(UUID id) {
        return xaTxApiTemplate.execute(() -> photoRepository.findById(id));
    }

    @Step("Найти фото пользователя {username} по описанию")
    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        return xaTxApiTemplate.execute(() -> photoRepository.findByUsernameAndDescription(username, description));
    }

    @Step("Найти фото пользователя {username} по стране {code}")
    public Optional<PhotoEntity> findByUsernameAndCountry(String username, String code) {
        return xaTxApiTemplate.execute(() -> photoRepository.findByUsernameAndCountry(username, code));
    }

    @Step("Получить все фото пользователя {username}")
    public List<PhotoEntity> findAllUserPhoto(String username) {
        return xaTxApiTemplate.execute(() -> photoRepository.findAllUserPhoto(username));
    }

    @Step("Удалить фото")
    public void removePhoto(PhotoEntity photo) {
        xaTxApiTemplate.execute(() -> {
            photoRepository.remove(photo);
            return null;
        });
    }
}
