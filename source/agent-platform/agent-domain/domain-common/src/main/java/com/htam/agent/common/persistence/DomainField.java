package com.htam.agent.common.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DomainField {
    String value() default "";

    DomainFieldFill fill() default DomainFieldFill.DEFAULT;

    boolean json() default false;

    boolean exist() default true;
}
