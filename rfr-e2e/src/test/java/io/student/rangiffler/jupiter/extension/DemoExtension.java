package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.test.demo.DemoValue;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.PreInterruptCallback;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public final class DemoExtension implements
    BeforeAllCallback,
    BeforeClassTemplateInvocationCallback,
    TestInstancePreConstructCallback,
    BeforeEachCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    AfterEachCallback,
    TestInstancePreDestroyCallback,
    AfterAllCallback,
    AfterClassTemplateInvocationCallback,
    PreInterruptCallback,
    ParameterResolver {
  private static final AtomicInteger NEXT = new AtomicInteger();
  private final int id;
  private boolean announced;

  public DemoExtension() {
    id = NEXT.incrementAndGet();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    trace("beforeAll", context);
  }

  @Override
  public void beforeClassTemplateInvocation(ExtensionContext context) {
    trace("beforeClassTemplateInvocation", context);
  }

  @Override
  public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
    trace("preConstructTestInstance " + factoryContext.getTestClass().getSimpleName(), context);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    trace("beforeEach", context);
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    trace("beforeTestExecution", context);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    trace("afterTestExecution", context);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    trace("afterEach", context);
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext context) {
    trace("preDestroyTestInstance", context);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    trace("afterAll", context);
  }

  @Override
  public void afterClassTemplateInvocation(ExtensionContext context) {
    trace("afterClassTemplateInvocation", context);
  }

  @Override
  public void beforeThreadInterrupt(PreInterruptContext preInterruptContext, ExtensionContext context) {
    trace("beforeThreadInterrupt " + preInterruptContext.getThreadToInterrupt().getName(), context);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    return parameter.getParameter().getType() == DemoValue.class;
  }

  @Override
  public DemoValue resolveParameter(ParameterContext parameter, ExtensionContext context) {
    trace("resolveParameter " + parameter.getIndex(), context);
    return new DemoValue("demo");
  }

  private void trace(String callback, ExtensionContext context) {
    if (!announced) {
      announced = true;
      System.out.println("DemoExtension#" + id + " instantiated");
    }
    System.out.println("DemoExtension#" + id + " invokes " + callback + " for " + place(context));
  }

  private String place(ExtensionContext context) {
    String method = context.getTestMethod().map(Method::getName).orElse("class");
    return context.getRequiredTestClass().getSimpleName() + "." + method;
  }
}
