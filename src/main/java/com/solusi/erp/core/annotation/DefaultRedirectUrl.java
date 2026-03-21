package com.solusi.erp.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable smart redirect on GET errors.
 * If value is empty, it will try to auto-detect from @RequestMapping at class level.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultRedirectUrl {
    String value() default "";
}
