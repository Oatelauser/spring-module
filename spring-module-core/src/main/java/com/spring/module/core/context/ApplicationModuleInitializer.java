package com.spring.module.core.context;

import com.abm.module.api.SpringModule;
import com.spring.module.core.event.StartingModuleEvent;
import com.spring.module.core.module.ModuleLifecycleProcessor;
import com.spring.module.core.process.BeanRegistryPostProcessor;
import org.springframework.context.*;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * 模块应用初始化器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-09
 * @since 1.0
 */
public class ApplicationModuleInitializer implements Ordered, ApplicationEventPublisherAware,
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final Set<Class<?>> REGISTER_BEANS = Set.of(
            SpringModule.class,
            ModuleLifecycleProcessor.class,
            BeanRegistryPostProcessor.class
    );

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        if (applicationContext instanceof AnnotationApplicationModuleContext applicationModuleContext) {
            this.notifyStartingModuleEvent(applicationModuleContext);
            this.initializeApplicationModuleContext(applicationModuleContext);
            this.registerInitializedBeans(applicationModuleContext);
            applicationModuleContext.invokeModulePostProcessors(applicationModuleContext.getSpringModule());
        }
    }

    protected void registerInitializedBeans(AnnotationApplicationModuleContext applicationModuleContext) {
        applicationModuleContext.registerSingletonBean(applicationModuleContext.getSpringModule());
        applicationModuleContext.register(ModuleLifecycleProcessor.class);
        applicationModuleContext.registerBean(BeanRegistryPostProcessor.class, applicationModuleContext);
    }

    protected void initializeApplicationModuleContext(AnnotationApplicationModuleContext applicationModuleContext) {
        ApplicationContext parent = applicationModuleContext.getRealParent();
        if (parent != null) {
            applicationModuleContext.initializeModuleApplication(parent);
        }
    }

    protected void notifyStartingModuleEvent(AnnotationApplicationModuleContext applicationModuleContext) {
        this.applicationEventPublisher.publishEvent(new StartingModuleEvent(applicationModuleContext));
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
