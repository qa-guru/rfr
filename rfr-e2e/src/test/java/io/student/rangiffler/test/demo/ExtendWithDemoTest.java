package io.student.rangiffler.test.demo;

import io.qameta.allure.Epic;
import io.student.rangiffler.jupiter.extension.DemoExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Места @ExtendWith")
@Epic("Demo")
class ExtendWithDemoTest {

  @Nested
  @ExtendWith(DemoExtension.class)
  @DisplayName("Класс")
  final class OnClass {

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Метод")
  final class OnMethod {

    @Test
    @ExtendWith(DemoExtension.class)
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @ExtendWith(DemoExtension.class)
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @WithDemoExtension
  @DisplayName("Составная аннотация")
  final class OnComposedAnnotation {

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Суперкласс")
  final class OnSuperclass extends RegisteredOnSuperclass {
  }

  @Nested
  @DisplayName("Интерфейс")
  final class OnInterface implements RegisteredOnInterface {
  }

  @Nested
  @DisplayName("Статическое поле")
  final class OnStaticField extends RegisteredOnStaticField {
  }

  @Nested
  @DisplayName("Поле экземпляра")
  final class OnInstanceField {

    @ExtendWith(DemoExtension.class)
    private final Object anchor = new Object();

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertNotNull(anchor);
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertNotNull(anchor);
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Параметр конструктора")
  final class OnConstructorParameter {

    private final DemoValue value;

    OnConstructorParameter(@ExtendWith(DemoExtension.class) DemoValue value) {
      this.value = value;
    }

    @Test
    @DisplayName("Первый тест")
    void runs() {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain() {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Параметр тестового метода")
  final class OnTestParameter {

    @Test
    @DisplayName("Первый тест")
    void runs(@ExtendWith(DemoExtension.class) DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(@ExtendWith(DemoExtension.class) DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Параметр @BeforeAll")
  final class OnBeforeAllParameter {

    private DemoValue value;

    @BeforeAll
    void beforeAll(@ExtendWith(DemoExtension.class) DemoValue resolved) {
      value = resolved;
    }

    @Test
    @DisplayName("Первый тест")
    void runs() {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain() {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Параметр @AfterAll")
  final class OnAfterAllParameter {

    @Test
    @DisplayName("Первый тест")
    void runs() {
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain() {
    }

    @AfterAll
    void afterAll(@ExtendWith(DemoExtension.class) DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Параметр @BeforeEach")
  final class OnBeforeEachParameter {

    private DemoValue value;

    @BeforeEach
    void beforeEach(@ExtendWith(DemoExtension.class) DemoValue resolved) {
      value = resolved;
    }

    @Test
    @DisplayName("Первый тест")
    void runs() {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain() {
      assertEquals("demo", value.source());
    }
  }

  @Nested
  @DisplayName("Параметр @AfterEach")
  final class OnAfterEachParameter {

    @Test
    @DisplayName("Первый тест")
    void runs() {
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain() {
    }

    @AfterEach
    void afterEach(@ExtendWith(DemoExtension.class) DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @ExtendWith(DemoExtension.class)
  abstract static class RegisteredOnSuperclass {

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  @ExtendWith(DemoExtension.class)
  interface RegisteredOnInterface {

    @Test
    @DisplayName("Первый тест")
    default void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    default void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }

  abstract static class RegisteredOnStaticField {

    @ExtendWith(DemoExtension.class)
    private static final Object anchor = new Object();

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertNotNull(anchor);
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertNotNull(anchor);
      assertEquals("demo", value.source());
    }
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @ExtendWith(DemoExtension.class)
  private @interface WithDemoExtension {
  }
}

@ExtendWith(DemoExtension.class)
@DisplayName("Внешний класс")
class OnEnclosingClass {

  @Nested
  @DisplayName("Вложенный класс без своей аннотации")
  final class Inner {

    @Test
    @DisplayName("Первый тест")
    void runs(DemoValue value) {
      assertEquals("demo", value.source());
    }

    @Test
    @DisplayName("Второй тест")
    void runsAgain(DemoValue value) {
      assertEquals("demo", value.source());
    }
  }
}
