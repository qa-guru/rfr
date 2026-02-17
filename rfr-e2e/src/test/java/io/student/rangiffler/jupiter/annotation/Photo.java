package io.student.rangiffler.jupiter.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Photo {
    String countryCode() default "";
    String description() default "";
    String src() default ""; // если пусто — берём что-то из classpath
    int likes() default 0;
}