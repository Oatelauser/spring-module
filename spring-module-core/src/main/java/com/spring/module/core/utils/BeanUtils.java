package com.spring.module.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.AliasRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static org.springframework.util.StringUtils.hasText;

/**
 * Bean Utils
 *
 * @author DearYang
 * @date 2022-07-29
 * @since 1.0
 */
@SuppressWarnings("unused")
public abstract class BeanUtils {

    private static final Log LOG = LogFactory.getLog(BeanUtils.class);

    /**
     * 判断注解标注的bean是否被注册
     *
     * @param registry       {@link BeanDefinitionRegistry}
     * @param annotatedClass bean class
     * @return 是否注册
     */
    public static boolean isPresentAnnotatedBean(BeanDefinitionRegistry registry, Class<?> annotatedClass) {
        ClassLoader classLoader = annotatedClass.getClassLoader();
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                continue;
            }

            AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
            String className = annotationMetadata.getClassName();
            Class<?> targetClass = ClassUtils.resolveClassName(className, classLoader);
            boolean present = ObjectUtils.nullSafeEquals(targetClass, annotatedClass);
            if (!present) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("The annotatedClass[" + annotatedClass
                        + "], bean name[" + beanName + "] was present in registry");
            }
            return true;
        }

        return false;
    }

    /**
     * 判断bean是否有别名
     *
     * @param registry 别民注册器
     * @param beanName bean名
     * @param alias    指定的bean别名
     * @return 是否有指定别民
     */
    public static boolean hasAlias(AliasRegistry registry, String beanName, String alias) {
        return hasText(beanName) && hasText(alias) && ObjectUtils.containsElement(registry.getAliases(beanName), alias);
    }

    /**
     * 通过指定类型的bean，去获取排序后的bean集合
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param type        bean类型
     * @param <T>         bean的泛型
     * @return bean集合
     */
    public static <T> List<T> sort(ListableBeanFactory beanFactory, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type);
        List<T> beansOfType = new ArrayList<>(beansMap.values());
        return sort(beansOfType);
    }

    /**
     * 指定bean集合进行排序
     *
     * @param beansOfType bean集合
     * @param <T>         bean类型
     * @return 排序后的集合
     */
    public static <T> List<T> sort(Collection<T> beansOfType) {
        List<T> beanTypes = beansOfType instanceof List ? (List<T>) beansOfType : new ArrayList<>(beansOfType);
        AnnotationAwareOrderComparator.sort(beanTypes);
        return beanTypes;
    }

    /**
     * 对Map集合beanName-bean的类型进行排序
     *
     * @param beansMap bean的map集合
     * @param <T>      bean类型
     * @return 排序后的map集合
     */
    public static <T> Map<String, T> sort(final Map<String, T> beansMap) {
        List<NamingBean<T>> namingBeans = new ArrayList<>(beansMap.size());
        for (Map.Entry<String, T> entry : beansMap.entrySet()) {
            String beanName = entry.getKey();
            T bean = entry.getValue();
            NamingBean<T> namingBean = new NamingBean<>(beanName, bean);
            namingBeans.add(namingBean);
        }

        AnnotationAwareOrderComparator.sort(namingBeans);
        Map<String, T> sortedBeansMap = new LinkedHashMap<>(beansMap.size());

        for (NamingBean<T> namingBean : namingBeans) {
            sortedBeansMap.put(namingBean.name, namingBean.getBean());
        }

        return sortedBeansMap;
    }

    /**
     * 获取带有泛型的Bean对象
     *
     * @param context  Bean上下文接口
     * @param clazz    Bean的类型
     * @param generics Bean的类型泛型
     * @param <T>      Bean的泛型
     * @return 具有泛型的Bean对象
     */
    @Nullable
    public static <T> T getBeanOfGenerics(ListableBeanFactory context, Class<?> clazz, Class<?>... generics) {
        ResolvableType type = ResolvableType.forClassWithGenerics(clazz, generics);
        return getBeanOfGenerics(context, type);
    }

    /**
     * @see BeanUtils#getBeanOfGenerics(ListableBeanFactory, ResolvableType)
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getBeanOfGenerics(ListableBeanFactory context, ResolvableType type) {
        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, type);
        for (String beanName : beanNames) {
            if (context.isTypeMatch(beanName, type)) {
                return (T) context.getBean(beanName);
            }
        }
        return null;
    }

    /**
     * 获取指定的bean对象集合
     *
     * @param context Bean上下文
     * @param clazz   Bean类型
     * @param <T>     bean对象的具体类型
     * @return bean对象集合
     */
    public static <T> List<T> getBeanOfList(ListableBeanFactory context, Class<T> clazz) {
        Map<String, T> beans = context.getBeansOfType(clazz);
        if (CollectionUtils.isEmpty(beans)) {
            return Collections.emptyList();
        }

        return new ArrayList<>(beans.values());
    }

    static class NamingBean<T> extends SortedBean<T> {
        private final String name;

        NamingBean(String name, T bean) {
            super(bean);
            this.name = name;
        }

    }

}
