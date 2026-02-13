package io.student.rangiffler.jupiter.extension;


import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.service.UserDbClient;
import io.student.rangiffler.service.UserDbClientHibernate;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Optional;

import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;
import static io.student.rangiffler.jupiter.extension.TestMethodContextExtension.context;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private final UserDbClientHibernate usersClient = new UserDbClientHibernate();
    private final Faker faker = new Faker();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                a -> {
                    if("".equals(a.username())){
                        final String username = faker.name().username();
                        var user = usersClient.createFullUser(username, STANDART_PASSWORD);
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                user
                        );
                    }
                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdUser().orElseThrow();
    }

    public static Optional<UserJson> createdUser() {
        final ExtensionContext methodContext = context();
        return Optional.ofNullable(
                methodContext.getStore(NAMESPACE)
                        .get(methodContext.getUniqueId(), UserJson.class)
        );
    }
}
