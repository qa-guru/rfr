package io.student.rangiffler.jupiter.extension;


import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.model.TestData;
import io.student.rangiffler.model.UserJson;
import io.student.rangiffler.service.UserDbClient;
import io.student.rangiffler.service.UserDbClientHibernate;
import net.datafaker.Faker;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.student.rangiffler.data.UserData.STANDART_PASSWORD;
import static io.student.rangiffler.data.UserData.randomUsername;
import static io.student.rangiffler.jupiter.extension.TestMethodContextExtension.context;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    private final UserDbClientHibernate usersClient = new UserDbClientHibernate();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    String username = userAnno.username().isBlank() ? randomUsername() : userAnno.username();

                    final UserJson user = usersClient.createFullUser(username, STANDART_PASSWORD);
                    final List<UserJson> incomes = usersClient.addIncomeInvitation(user, userAnno.incomeInvitations());
                    final List<UserJson> outcomes = usersClient.addOutcomeInvitation(user, userAnno.outcomeInvitations());
                    final List<UserJson> friends = usersClient.addFriend(user, userAnno.friends());

                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            user.addTestData(new TestData(STANDART_PASSWORD, incomes, outcomes, friends, new ArrayList<>()))
                    );
                });
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
