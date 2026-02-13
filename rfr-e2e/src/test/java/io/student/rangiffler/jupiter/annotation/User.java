package io.student.rangiffler.jupiter.annotation;

import io.student.rangiffler.jupiter.extension.PhotoExtension;
import io.student.rangiffler.jupiter.extension.UserExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith({UserExtension.class, PhotoExtension.class})
public @interface User {
    String username() default "";
    Photo[] photos() default {};
}
