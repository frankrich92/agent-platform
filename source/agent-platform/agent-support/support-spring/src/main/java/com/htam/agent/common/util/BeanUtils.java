package com.htam.agent.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring bean and object-copy helper for framework-aware modules.
 */
@Component
public final class BeanUtils implements BeanFactoryPostProcessor {
    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanUtils.beanFactory = beanFactory;
    }

    public static boolean checkBeanFactory() {
        return beanFactory != null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    public static <T> T getBean(Class<T> clz) throws BeansException {
        return beanFactory.getBean(clz);
    }

    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }

    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getAliases(name);
    }

    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return beanFactory.getBeansWithAnnotation(annotationType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    public static <T> T copy(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        T target = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T, S> List<T> copyList(List<S> sourceList, Class<T> clazz) {
        if (sourceList == null) {
            return new ArrayList<>();
        }
        return sourceList.stream().map(source -> copy(source, clazz)).collect(Collectors.toList());
    }

    public static <T, S> IPage<T> copyPage(IPage<S> sourcePage, Class<T> clazz) {
        if (sourcePage == null) {
            return null;
        }
        IPage<T> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        targetPage.setRecords(copyList(sourcePage.getRecords(), clazz));
        return targetPage;
    }
}
