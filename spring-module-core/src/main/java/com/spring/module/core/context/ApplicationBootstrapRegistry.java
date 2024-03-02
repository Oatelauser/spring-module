package com.spring.module.core.context;

import com.abm.module.api.SpringModule;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;

import static com.spring.module.core.context.ApplicationModuleInitializer.REGISTER_BEANS;
import static com.spring.module.core.exception.ModuleException.alreadyExistBeanName;

/**
 * 模块应用引导启动注册器
 * <p>
 * 1.注册初始
 * 2.获取父类上下文的Bean
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-09
 * @since 1.0
 */
public class ApplicationBootstrapRegistry implements BootstrapRegistryInitializer {

    private final SpringModule springModule;

    public ApplicationBootstrapRegistry(SpringModule springModule) {
        this.springModule = springModule;
    }

    @Override
    public void initialize(BootstrapRegistry registry) {
        registry.register(SpringModule.class, BootstrapRegistry.InstanceSupplier.of(this.springModule));
        registry.addCloseListener(event -> {
            ConfigurableApplicationContext applicationContext = event.getApplicationContext();
            if (applicationContext instanceof AnnotationApplicationModuleContext applicationModuleContext) {
                this.registerParentBeans(applicationModuleContext);
            }
        });
    }

    private void registerParentBeans(AnnotationApplicationModuleContext applicationContext) {
        List<AnnotationConfigApplicationContext> parents = applicationContext.getParents();
        if (!CollectionUtils.isEmpty(parents)) {
            Map<String, String> beanNames = new HashMap<>();
            parents.forEach(parent -> this.registerParentBeans(beanNames, parent, applicationContext));
        }
    }

    protected void registerParentBeans(Map<String, String> beanNames,
            AnnotationConfigApplicationContext parent, AnnotationApplicationModuleContext applicationContext) {
        String[] beanDefinitionNames = parent.getBeanDefinitionNames();
        Set<String> filteringBeanNames = getBootstrapFilteringBeanNames(parent);
        for (String beanName : beanDefinitionNames) {
            if (filteringBeanNames.contains(beanName)) {
                continue;
            }
            BeanDefinition beanDefinition = parent.getBeanDefinition(beanName);
            if (this.filteringBeanName(beanName, beanDefinition, parent)) {
                continue;
            }

            String sourceModuleName;
            String targetModuleName = parent.getId();
            if ((sourceModuleName = beanNames.put(beanName, targetModuleName)) != null) {
                alreadyExistBeanName("模块[" + sourceModuleName + "]和模块["
                        + targetModuleName + "]存在相同的Bean名称[" + beanName + "]冲突");
            }

            Object bean = parent.containsBean(beanName) ? parent.getBean(beanName) : null;
            this.registerParentBeans(parent.getId(), beanName, beanDefinition, bean, applicationContext);
        }
    }

    private void registerParentBeans(String sourceModuleName, String beanName, BeanDefinition beanDefinition,
            Object bean, AnnotationApplicationModuleContext applicationContext) {
        beanDefinition.setAttribute(Objects.requireNonNull(applicationContext.getId()), sourceModuleName);
        applicationContext.registerBeanDefinition(beanName, beanDefinition);
        if (bean != null && beanDefinition.isSingleton()) {
            applicationContext.registerSingletonBean(beanName, bean);
        }
    }

    protected boolean filteringBeanName(String beanName, BeanDefinition beanDefinition,
            AnnotationConfigApplicationContext applicationContext) {
        ClassLoader classLoader = Objects.requireNonNull(applicationContext.getClassLoader());
        try {
            Class<?> beanClass = resolveBeanClass(beanName, beanDefinition, applicationContext);
            if (beanClass != null) {
                beanClass = ClassUtils.getUserClass(beanClass);
                return !classLoader.equals(beanClass.getClassLoader());
            }
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static Class<?> resolveBeanClass(String beanName, BeanDefinition beanDefinition,
            AnnotationConfigApplicationContext applicationContext) throws ClassNotFoundException {
        if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition
                && rootBeanDefinition.hasBeanClass()) {
            return rootBeanDefinition.getBeanClass();
        }
        //if (applicationContext.containsBean(beanName)) {
        //    Object bean = applicationContext.getBean(beanName);
        //    return bean.getClass();
        //}
        Class<?> beanClass = applicationContext.getType(beanName);
        if (beanClass != null) {
            return ClassUtils.getUserClass(beanClass);
        }
        if (beanDefinition instanceof AbstractBeanDefinition instanceBeanDefinition) {
            Supplier<?> instanceSupplier = instanceBeanDefinition.getInstanceSupplier();
            if (instanceSupplier != null) {
                return instanceSupplier.get().getClass();
            }
        }

        ClassLoader classLoader = Objects.requireNonNull(applicationContext.getClassLoader());
        String className = beanDefinition.getBeanClassName();
        if (StringUtils.hasText(className)) {
            return classLoader.loadClass(className);
        }
        Object source = beanDefinition.getSource();
        if (source instanceof MethodMetadata metadata && StringUtils.hasText(metadata.getReturnTypeName())) {
            return classLoader.loadClass(metadata.getReturnTypeName());
        }
        return null;
    }

    /**
     * 提供白名单的bean名称过滤
     *
     * @param applicationContext 应用上下文
     * @return 过滤白名单
     */
    public static Set<String> getBootstrapFilteringBeanNames(AnnotationConfigApplicationContext applicationContext) {
        Set<String> filteringBeanNames = new HashSet<>();
        for (Class<?> registerBean : REGISTER_BEANS) {
            Collections.addAll(filteringBeanNames, applicationContext.getBeanNamesForType(registerBean));
        }
        if (applicationContext instanceof AnnotationApplicationModuleContext applicationModuleContext) {
            Class<?> applicationClass = applicationModuleContext.getMainApplicationClass();
            if (applicationClass != null) {
                Collections.addAll(filteringBeanNames, applicationContext.getBeanNamesForType(applicationClass));
            }
        }
        return filteringBeanNames;
    }

}
