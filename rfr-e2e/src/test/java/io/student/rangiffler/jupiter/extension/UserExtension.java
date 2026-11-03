package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

public final class UserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index = 0; index < parameters.length; index++) {
      if (!parameters[index].isAnnotationPresent(User.class)) {
        continue;
      }
      if (parameters[index].getType() != TestUser.class) {
        continue;
      }
      UserFixture fixture = new UserFixture(TestUser.unique());
      context.getStore(NAMESPACE).put(key(index), fixture);
      fixture.create();
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    return parameter.getParameter().getType() == TestUser.class
        && parameter.isAnnotated(User.class)
        && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameter, ExtensionContext context) {
    UserFixture fixture = context.getStore(NAMESPACE).get(key(parameter.getIndex()), UserFixture.class);
    if (fixture == null) {
      throw new ParameterResolutionException(
          "No user was created for parameter " + parameter.getIndex() + " of " + context.getRequiredTestMethod()
      );
    }
    return fixture.user();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Parameter[] parameters = context.getRequiredTestMethod().getParameters();
    for (int index = 0; index < parameters.length; index++) {
      if (!parameters[index].isAnnotationPresent(User.class)) {
        continue;
      }
      UserFixture fixture = context.getStore(NAMESPACE).remove(key(index), UserFixture.class);
      if (fixture != null) {
        fixture.close();
      }
    }
  }

  private static String key(int index) {
    return "user-" + index;
  }
}
