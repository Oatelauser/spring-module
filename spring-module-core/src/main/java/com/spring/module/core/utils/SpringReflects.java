package com.spring.module.core.utils;

import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

/**
 * Spring源码的方法反射
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-15
 * @since 1.0
 */
public abstract class SpringReflects {

    /**
     * org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor#postProcessEnvironment
     */
    public static final Method PROCESS_ENVIRONMENT_METHOD;

    static {
        PROCESS_ENVIRONMENT_METHOD = reflectEnvMethod();
    }

    static Method reflectEnvMethod() {
        Method method = ReflectionUtils.findMethod(ConfigDataEnvironmentPostProcessor.class,
                "postProcessEnvironment", ConfigurableEnvironment.class, ResourceLoader.class, Collection.class);
        Objects.requireNonNull(method);
        ReflectionUtils.makeAccessible(method);
        return method;
    }

    /**
     * 反射获取对象的字段
     *
     * @param fieldName 成员变量名
     * @param obj       实例对象
     * @param <T>       返回的字段类型
     * @return 成员变量
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(String fieldName, Object obj) {
        boolean staticField = false;
        Class<?> type = obj.getClass();
        if (obj instanceof Class<?> objClass) {
            type = objClass;
            staticField = true;
        }
        Field field = ReflectionUtils.findField(type, fieldName);
        Objects.requireNonNull(field);
        ReflectionUtils.makeAccessible(field);
        return (T) ReflectionUtils.getField(field, staticField ? null : obj);
    }

    /**
     * 反射设置字段的值
     *
     * @param fieldName 成员变量名
     * @param obj       实例对象
     * @param val       字段值
     */
    public static void setField(String fieldName, Object obj, Object val) {
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        Objects.requireNonNull(field);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, val);
    }


}
