package com.htam.agent.repo.mybatis.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.handlers.AnnotationHandler;
import com.htam.agent.common.persistence.DomainField;
import com.htam.agent.common.persistence.DomainFieldFill;
import com.htam.agent.common.persistence.DomainId;
import com.htam.agent.common.persistence.DomainIdType;
import com.htam.agent.common.persistence.DomainTable;
import com.htam.agent.repo.mybatis.type.JsonNodeTypeHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.stereotype.Component;

@Component
public class DomainMybatisAnnotationHandler implements AnnotationHandler {

    @Override
    public <T extends Annotation> T getAnnotation(Class<?> beanClass, Class<T> annotationClass) {
        T annotation = AnnotationHandler.super.getAnnotation(beanClass, annotationClass);
        if (annotation != null) {
            return annotation;
        }
        if (annotationClass == TableName.class) {
            DomainTable domainTable = beanClass.getAnnotation(DomainTable.class);
            if (domainTable != null) {
                return annotationClass.cast(annotation(
                        TableName.class,
                        Map.of(
                                "value", domainTable.value(),
                                "autoResultMap", domainTable.autoResultMap(),
                                "excludeProperty", domainTable.excludeProperty())));
            }
        }
        return null;
    }

    @Override
    public <T extends Annotation> boolean isAnnotationPresent(Class<?> beanClass, Class<T> annotationClass) {
        return getAnnotation(beanClass, annotationClass) != null;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        T annotation = AnnotationHandler.super.getAnnotation(field, annotationClass);
        if (annotation != null) {
            return annotation;
        }
        if (annotationClass == TableId.class) {
            DomainId domainId = field.getAnnotation(DomainId.class);
            if (domainId != null) {
                return annotationClass.cast(annotation(
                        TableId.class,
                        Map.of("value", domainId.value(), "type", toMybatisIdType(domainId.type()))));
            }
        }
        if (annotationClass == TableField.class) {
            DomainField domainField = field.getAnnotation(DomainField.class);
            if (domainField != null) {
                return annotationClass.cast(annotation(
                        TableField.class,
                        Map.of(
                                "value", domainField.value(),
                                "fill", toMybatisFieldFill(domainField.fill()),
                                "exist", domainField.exist(),
                                "typeHandler", typeHandler(domainField))));
            }
        }
        return null;
    }

    @Override
    public <T extends Annotation> boolean isAnnotationPresent(Field field, Class<T> annotationClass) {
        return getAnnotation(field, annotationClass) != null;
    }

    private static IdType toMybatisIdType(DomainIdType idType) {
        return IdType.valueOf(idType.name());
    }

    private static FieldFill toMybatisFieldFill(DomainFieldFill fill) {
        return FieldFill.valueOf(fill.name());
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends TypeHandler<?>> typeHandler(DomainField field) {
        if (field.json()) {
            return (Class<? extends TypeHandler<?>>) (Class<?>) JsonNodeTypeHandler.class;
        }
        return org.apache.ibatis.type.UnknownTypeHandler.class;
    }

    private static <T extends Annotation> T annotation(Class<T> type, Map<String, Object> values) {
        InvocationHandler handler = (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("annotationType") && method.getParameterCount() == 0) {
                return type;
            }
            if (methodName.equals("toString") && method.getParameterCount() == 0) {
                return type.getName() + values;
            }
            if (methodName.equals("hashCode") && method.getParameterCount() == 0) {
                return type.hashCode() * 31 + values.hashCode();
            }
            if (methodName.equals("equals") && method.getParameterCount() == 1) {
                return proxy == args[0];
            }
            if (values.containsKey(methodName)) {
                return values.get(methodName);
            }
            Object defaultValue = method.getDefaultValue();
            if (defaultValue != null) {
                return defaultValue;
            }
            throw new IllegalStateException("No value for " + type.getName() + "." + methodName);
        };
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
        return type.cast(proxy);
    }
}
