package org.yeauty.annotation;

import org.yeauty.support.MethodArgumentResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnMessage {
    Class<? extends MethodArgumentResolver>[] addResolver() default {};
}