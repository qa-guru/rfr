package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.jupiter.annotation.Photo;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.PhotoJson;
import io.student.rangiffler.model.TestData;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.service.PhotoDbClientHibernate;
import io.student.rangiffler.service.UserDbClientHibernate;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.*;

import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;


public class PhotoExtension implements BeforeEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtension.class);

    private final UserDbClientHibernate userClient = new UserDbClientHibernate();
    private final PhotoDbClientHibernate photoClient = new PhotoDbClientHibernate();
    private final Faker faker = new Faker();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnno -> {
            Photo[] photos = userAnno.photos();
            if (photos.length == 0) {
                return;
            }

            Optional<UserJson> maybeUser = UserExtension.createdUser();

            UserJson user;

            if (maybeUser.isPresent()) {
                user = maybeUser.get();
            } else {
                String username = "".equals(userAnno.username())
                        ? faker.name().username()
                        : userAnno.username();

                user = userClient.createFullUser(username, STANDART_PASSWORD);

                context.getStore(NAMESPACE)
                        .put(context.getUniqueId(), user);
            }

            List<PhotoJson> createdPhotos = new ArrayList<>();
            for (Photo photoAnno : photos) {
                PhotoEntity entity = buildPhotoEntity(user, photoAnno);
                photoClient.createPhoto(entity);
                createdPhotos.add(
                        new PhotoJson(
                                photoAnno.src(),
                                photoAnno.countryCode(),
                                photoAnno.description(),
                                photoAnno.likes()
                        )
                );
            }

            TestData updatedTestData = user.testData() == null
                    ? new TestData(createdPhotos)
                    : user.testData().addPhotos(createdPhotos);

            UserJson updatedUser = user.addTestData(updatedTestData);

            context.getStore(NAMESPACE)
                    .put(context.getUniqueId(), updatedUser);
        });
    }

    private PhotoEntity buildPhotoEntity(UserJson user, Photo photoAnno) {
        UUID userId = UUID.fromString(user.data().user().id());

        UserEntity fullUser = userClient.findById(userId)
                .orElseThrow();

        PhotoEntity pe = new PhotoEntity();
        pe.setUser(fullUser);
        pe.setCountry(fullUser.getCountry());
        pe.setDescription(photoAnno.description());
        pe.setPhoto(loadPhotoBytes(photoAnno.src()));
        pe.setCreatedDate(new Date());

        return pe;
    }

    private UUID resolveCountryId(String code) {
        return UUID.fromString("11f0e273-0587-1c64-ac58-0242ac110002");
    }

    private byte[] loadPhotoBytes(String src) {
        return new byte[0];
    }
}