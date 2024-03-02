package com.spring.module.core.module;

import com.abm.module.api.SpringModule;
import com.spring.module.core.context.*;
import com.spring.module.core.loading.JarClassLoader;
import com.spring.module.core.loading.ZipClassLoader;
import com.spring.module.core.parse.ModuleNameResolver;
import com.spring.module.core.utils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 模块注册的支持
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class ModulesRegistrarSupport implements ApplicationContextAware, SmartInitializingSingleton {

    public static final ApplicationContextInitializer<?>[] APPLICATION_CONTEXT_INITIALIZERS;

    ModuleNameResolver moduleNameResolver;
    private SpringApplicationCreater applicationCreater;
    protected ConfigurableApplicationContext applicationContext;
    private ApplicationContextFactory applicationContextFactory;
    private BootstrapRegistryInitializer bootstrapRegistryInitializer;
    private ApplicationContextInitializer<ConfigurableApplicationContext> applicationContextInitializer;

    static {
        try {
            Class<?> initializerClass = Class.forName("org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer");
            Constructor<?> constructor = initializerClass.getDeclaredConstructor();
            ReflectionUtils.makeAccessible(constructor);
            ApplicationContextInitializer<?> readerFactoryContextInitializer =
                    (ApplicationContextInitializer<?>) constructor.newInstance();
            APPLICATION_CONTEXT_INITIALIZERS = new ApplicationContextInitializer<?>[]{
                    new ConditionEvaluationReportLoggingListener(),
                    readerFactoryContextInitializer
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 构建模块的上下文，并且刷新
     *
     * @param springModule        模块加载的信息
     * @param resourceLoader      {@link DefaultResourceLoader}
     * @param applicationContexts 模块上下文的所有父类上下文
     * @return 加载的模块上下文
     */
    protected AnnotationApplicationModuleContext createApplicationModuleContext(SpringModule springModule,
            DefaultResourceLoader resourceLoader, List<AnnotationApplicationModuleContext> applicationContexts) {
        String moduleName = springModule.getModuleName();
        Class<?> source = applicationCreater.createVirtualApplication(springModule.getProjectMetadata(), resourceLoader.getClassLoader());
        BootstrapRegistryInitializer bootstrapRegistryInitializer = this.bootstrapRegistryInitializer == null ?
                new ApplicationBootstrapRegistry(springModule) : this.bootstrapRegistryInitializer;
        return (AnnotationApplicationModuleContext) new SpringModuleApplicationBuilder(moduleName, resourceLoader, source)
                .addParents(applicationContexts)
                .springModule(springModule)
                .main(source)
                .web(WebApplicationType.NONE)
                .parent(this.applicationContext)
                .contextFactory(new ApplicationModuleApplicationContextFactory())
                //.environment(this.applicationContext.getEnvironment())  // 不能使用同一个环境，否则会产生类加载器的泄漏
                .initializers(APPLICATION_CONTEXT_INITIALIZERS)
                .initializers(this.applicationContextInitializer)
                .addBootstrapRegistryInitializer(bootstrapRegistryInitializer)
                .logStartupInfo(true)
                .bannerMode(Banner.Mode.OFF)
                .build()
                .run();
    }

    protected AnnotationApplicationModuleContext createApplicationModuleContext(SpringModule springModule) {
        if (!(springModule.getClassLoader() instanceof ZipClassLoader classLoader)) {
            throw new IllegalStateException("模块类加载器的类型不对：" + springModule);
        }

        List<String> parentModuleNames = this.mergeModules(springModule.getParentModules());
        List<AnnotationApplicationModuleContext> applicationContexts = new ArrayList<>();
        for (String parentModuleName : parentModuleNames) {
            AnnotationApplicationModuleContext parentContext = this.applicationContext(parentModuleName);
            classLoader.addParent((JarClassLoader) parentContext.getClassLoader());
            applicationContexts.add(parentContext);
        }

        return this.createApplicationModuleContext(springModule,
                new DefaultResourceLoader(classLoader), applicationContexts);
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (applicationCreater == null) {
            this.applicationCreater = this.applicationContext.getBean(SpringApplicationCreater.class);
        }
        if (this.moduleNameResolver == null) {
            this.moduleNameResolver = this.applicationContext.getBean(ModuleNameResolver.class);
        }
        if (this.applicationContextInitializer == null) {
            this.applicationContextInitializer = BeanUtils.getBeanOfGenerics(this.applicationContext,
                    ApplicationContextInitializer.class, ConfigurableApplicationContext.class);
        }
        if (this.applicationContextFactory == null) {
            this.applicationContextFactory = this.applicationContext.getBean(ApplicationContextFactory.class);
        }
        if (this.bootstrapRegistryInitializer == null) {
            this.bootstrapRegistryInitializer = this.applicationContext
                    .getBeanProvider(BootstrapRegistryInitializer.class).getIfAvailable();
        }
    }

    /**
     * 通过模块的元数据信息获取应用上下文
     * <p>
     * 用于方便调用，再这里抽象该方法
     *
     * @param moduleName 模块名
     * @return 模块应用上下文
     */
    protected abstract AnnotationApplicationModuleContext applicationContext(String moduleName);

    /**
     * 合并模块，只保留没有关系的顶级模块
     *
     * @param modules 所有的模块名
     * @return 只保留没有关系的顶级模块
     */
    protected abstract List<String> mergeModules(List<String> modules);

    public SpringApplicationCreater getApplicationCreater() {
        return applicationCreater;
    }

    public void setApplicationCreater(SpringApplicationCreater applicationCreater) {
        this.applicationCreater = applicationCreater;
    }

    public ModuleNameResolver getModuleNameResolver() {
        return moduleNameResolver;
    }

    public void setModuleNameResolver(ModuleNameResolver moduleNameResolver) {
        this.moduleNameResolver = moduleNameResolver;
    }

    public BootstrapRegistryInitializer getApplicationBootstrapRegistry() {
        return bootstrapRegistryInitializer;
    }

    public void setApplicationBootstrapRegistry(BootstrapRegistryInitializer bootstrapRegistryInitializer) {
        this.bootstrapRegistryInitializer = bootstrapRegistryInitializer;
    }

    public ApplicationContextInitializer<ConfigurableApplicationContext> getApplicationContextInitializer() {
        return applicationContextInitializer;
    }

    public void setApplicationContextInitializer(ApplicationContextInitializer<ConfigurableApplicationContext> applicationContextInitializer) {
        this.applicationContextInitializer = applicationContextInitializer;
    }

    public ApplicationContextFactory getApplicationContextFactory() {
        return applicationContextFactory;
    }

    public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
        this.applicationContextFactory = applicationContextFactory;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

}
