package io.student.rangiffler.jupiter.annotation;

import com.microsoft.playwright.junit.UsePlaywright;
import io.qameta.allure.jupiter.AllureJupiter;
import io.student.rangiffler.playwright.PlaywrightOptionsFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@UsePlaywright(PlaywrightOptionsFactory.class)
@ExtendWith(AllureJupiter.class)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebTest {
}
