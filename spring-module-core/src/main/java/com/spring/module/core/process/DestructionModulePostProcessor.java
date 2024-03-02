package com.spring.module.core.process;

import com.spring.module.core.context.AnnotationApplicationModuleContext;
import com.spring.module.core.module.DisposableModule;
import com.spring.module.tools.utils.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cglib.core.AbstractClassGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块销毁的清理工作：防止模块卸载后的内存泄漏
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-12
 * @since 1.0
 */
public class DestructionModulePostProcessor implements DisposableModule, Ordered {

    private static final Map<Method, String> beanNameCache;
    private static final Map<Method, Boolean> scopedProxyCache;
    public static final Map<ClassLoader, Map<String, SpringFactoriesLoader>> CLASSLOADER_CACHE;

    static {
        CLASSLOADER_CACHE = Reflections.getField("cache", SpringFactoriesLoader.class);
        Class<?> beanAnnotationHelperClass = ClassUtils.resolveClassName("org.springframework.context.annotation.BeanAnnotationHelper", null);
        beanNameCache = Reflections.getField("beanNameCache", beanAnnotationHelperClass);
        scopedProxyCache = Reflections.getField("scopedProxyCache", beanAnnotationHelperClass);
    }

    @Override
    public void destroy(ApplicationContext applicationContext) {
        // 1.解决SpringFactoriesLoader缓存子应用上下文类加载器
        this.clearGlobalSpringFactories(applicationContext);
        if (applicationContext instanceof AnnotationApplicationModuleContext applicationModuleContext) {
            ApplicationContext rootContext = applicationModuleContext.getRootApplicationContext();
            // 2.解决父类上下文监听器保存子应用上下文
            this.requiresDestructionListeners(rootContext);
            // 3.解决子上下文的配置类方法缓存
            this.requiresDestructionBeanAnnotationHelper(applicationModuleContext);
            // 4.解决AbstractClassGenerator的全局缓存
            this.requiresDestructionClassGeneratorCache(applicationModuleContext);
        }
    }

    /**
     * 清理{@link SpringFactoriesLoader}全局缓存的子类类加载器
     */
    protected void clearGlobalSpringFactories(ApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        CLASSLOADER_CACHE.remove(classLoader);
    }

    /**
     * 清理父类应用上下文缓存的子类监听器
     */
    protected void requiresDestructionListeners(ApplicationContext applicationContext) {
        List<ApplicationListener<?>> applicationListeners = new ArrayList<>();
        if (applicationContext instanceof AbstractApplicationContext baseContext) {
            for (ApplicationListener<?> applicationListener : baseContext.getApplicationListeners()) {
                String listenerClass = applicationListener.getClass().getName();
                if (listenerClass.startsWith(getClass().getName())) {
                    applicationListeners.add(applicationListener);
                }
            }
            applicationListeners.forEach(baseContext::removeApplicationListener);
        }
    }

    /**
     * 清理{@code org.springframework.context.annotation.BeanAnnotationHelper}缓存的方法对象
     */
    @SuppressWarnings("ConstantConditions")
    protected void requiresDestructionBeanAnnotationHelper(AnnotationApplicationModuleContext applicationModuleContext) {
        BeanRegistryPostProcessor beanRegistryPostProcessor = applicationModuleContext
                .getBean(BeanRegistryPostProcessor.class);

        Map<String, MultiValueMap<String, Method>> configMethods = new HashMap<>();
        for (BeanDefinition configBean : beanRegistryPostProcessor.configBeans) {
            String factoryMethodName = configBean.getFactoryMethodName();
            MultiValueMap<String, Method> methods = configMethods.computeIfAbsent(factoryMethodName, key -> {
                Class<?> configClass = applicationModuleContext.getType(configBean.getFactoryBeanName());
                configClass = ClassUtils.getUserClass(configClass);
                MultiValueMap<String, Method> configBeanMethods = new LinkedMultiValueMap<>();
                for (Method method : configClass.getDeclaredMethods()) {
                    configBeanMethods.add(method.getName(), method);
                }
                return configBeanMethods;
            });

            List<Method> configBeanMethods = methods.get(factoryMethodName);
            if (!CollectionUtils.isEmpty(configBeanMethods)) {
                configBeanMethods.forEach(method -> {
                    beanNameCache.remove(method);
                    scopedProxyCache.remove(method);
                });
            }
        }
    }

    /**
     * 清理{@code AbstractClassGenerator#CACHE}全局缓存
     */
    protected void requiresDestructionClassGeneratorCache(AnnotationApplicationModuleContext applicationModuleContext) {
        Map<ClassLoader, ?> classGeneratorCache = Reflections.getField("CACHE", AbstractClassGenerator.class);
        classGeneratorCache.remove(applicationModuleContext.getClassLoader());
        Class<?> applicationClass = applicationModuleContext.getMainApplicationClass();
        if (applicationClass != null) {
            classGeneratorCache.remove(applicationClass.getClassLoader());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
