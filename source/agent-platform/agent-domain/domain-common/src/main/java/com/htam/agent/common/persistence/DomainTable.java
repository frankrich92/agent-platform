package com.htam.agent.common.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainTable {
    String value() default "";

    boolean autoResultMap() default false;

    String[] excludeProperty() default {};
}
