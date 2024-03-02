package com.spring.module.core.context;

import com.abm.module.api.SpringModule;
import com.spring.module.core.config.SpringModuleEnvironmentPostProcessorsFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessorsFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 拓展{@link SpringApplication}新增应用ID
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
public class SpringModuleApplicationBuilder extends SpringApplicationBuilder {

    private SpringModuleApplication springApplication;

    @SuppressWarnings("all")
    public SpringModuleApplicationBuilder(String applicationId, Class<?>... sources) {
        super(sources);
        this.springApplication.setApplicationId(applicationId);
    }

    @SuppressWarnings("all")
    public SpringModuleApplicationBuilder(String applicationId, ResourceLoader resourceLoader, Class<?>... sources) {
        super(resourceLoader, sources);
        this.springApplication.setApplicationId(applicationId);
    }

    public SpringModuleApplicationBuilder addParents(List<AnnotationApplicationModuleContext> parents) {
        if (!CollectionUtils.isEmpty(parents)) {
            this.springApplication.setParents(parents);
        }
        return this;
    }

    public SpringModuleApplicationBuilder springModule(SpringModule springModule) {
        this.springApplication.setSpringModule(springModule);
        return this;
    }

    @Override
    protected SpringApplication createSpringApplication(ResourceLoader resourceLoader, Class<?>... sources) {
        return this.springApplication = new SpringModuleApplication(resourceLoader, sources);
    }

    static class SpringModuleApplication extends SpringApplication {

        private SpringModule springModule;
        private List<AnnotationApplicationModuleContext> parents;

        public SpringModuleApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
            super(resourceLoader, primarySources);
        }

        @Override
        protected ConfigurableApplicationContext createApplicationContext() {
            ConfigurableApplicationContext targetApplicationContext = super.createApplicationContext();
            if (targetApplicationContext instanceof AnnotationApplicationModuleContext applicationContext) {
                if (!CollectionUtils.isEmpty(parents)) {
                    parents.forEach(applicationContext::addParent);
                }
                applicationContext.setSpringModule(this.springModule);
                Class<?> applicationClass = this.getMainApplicationClass();
                if (applicationClass != null) {
                    applicationContext.setMainApplicationClass(applicationClass);
                }

                return applicationContext;
            }

            throw new IllegalStateException("不支持的模块上下文类型：" + targetApplicationContext);
        }

        @Override
        public ConfigurableApplicationContext run(String... args) {
            this.processEnvironmentListener();
            return super.run(args);
        }

        protected void processEnvironmentListener() {
            Set<ApplicationListener<?>> listeners = this.getListeners();
            if (!CollectionUtils.isEmpty(listeners)) {
                listeners.removeIf(listener -> EnvironmentPostProcessorApplicationListener.class
                        .equals(listener.getClass()));
                ClassLoader classLoader = this.getClassLoader();
                EnvironmentPostProcessorsFactory environmentPostProcessorsFactory = new SpringModuleEnvironmentPostProcessorsFactory(classLoader);
                listeners.add(EnvironmentPostProcessorApplicationListener.with(environmentPostProcessorsFactory));
                this.setListeners(listeners);
            }
        }

        public void setApplicationId(String applicationId) {
            ContextIdApplicationModuleContextInitializer contextInitializer = new ContextIdApplicationModuleContextInitializer(applicationId);
            Set<ApplicationContextInitializer<?>> contextInitializers = this.getInitializers();
            if (CollectionUtils.isEmpty(contextInitializers)) {
                this.setInitializers(List.of(contextInitializer));
                return;
            }

            List<ApplicationContextInitializer<?>> initializers = new ArrayList<>(contextInitializers);
            for (int i = 0; i < initializers.size(); i++) {
                ApplicationContextInitializer<?> initializer = initializers.get(i);
                if (ContextIdApplicationContextInitializer.class.equals(initializer.getClass())) {
                    initializers.set(i, contextInitializer);
                    break;
                }
            }
            this.setInitializers(initializers);
        }

        public List<AnnotationApplicationModuleContext> getParents() {
            return parents;
        }

        public void setParents(List<AnnotationApplicationModuleContext> parents) {
            this.parents = parents;
        }

        public void setSpringModule(SpringModule springModule) {
            this.springModule = springModule;
        }

        public SpringModule getSpringModule() {
            return springModule;
        }
    }

    static class ContextIdApplicationModuleContextInitializer extends ContextIdApplicationContextInitializer {

        private final String id;

        ContextIdApplicationModuleContextInitializer(String id) {
            Assert.hasText(id, "id不能为空字符串");
            this.id = id;
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            if (StringUtils.hasText(id)) {
                applicationContext.setId(id);
                if (applicationContext instanceof AbstractApplicationContext context) {
                    context.setDisplayName(id);
                }
            }
        }
    }

}
