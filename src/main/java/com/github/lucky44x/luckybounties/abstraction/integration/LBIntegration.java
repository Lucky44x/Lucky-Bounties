package com.github.lucky44x.luckybounties.abstraction.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LBIntegration {
    LoadTime value() default LoadTime.RUNTIME;
    String errorMessage() default "";

    enum LoadTime{
        RUNTIME,
        LOAD
    }
}
