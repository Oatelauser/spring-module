package com.spring.module.core.config;

import com.spring.module.core.loading.ArrayEnumeration;
import com.spring.module.core.loading.JarClassLoader;
import com.spring.module.core.loading.SpringClassLoader;
import com.spring.module.tools.utils.JarFilePaths;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessorsFactory;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import static com.spring.module.core.process.DestructionModulePostProcessor.CLASSLOADER_CACHE;
import static com.spring.module.core.utils.SpringReflects.PROCESS_ENVIRONMENT_METHOD;

/**
 * 支持模块上下文加载应用配置机制
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-13
 * @code classpath:META-INF/spring.factories
 * @code com.spring.module.core.context.SpringModuleApplicationBuilder.SpringModuleApplication#processEnvironmentListener
 * @see StarterConfigDataLocationResolver
 * @since 1.0
 */
public class SpringModuleEnvironmentPostProcessorsFactory implements EnvironmentPostProcessorsFactory {

    private static final URL classResource;

    static {
        try {
            Class<?> targetClass = SpringModuleEnvironmentPostProcessorsFactory.class;
            File file = JarFilePaths.resolveJarPath(targetClass);
            classResource = new File(file, "META-INF/module.factories").toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ClassLoader classLoader;

    public SpringModuleEnvironmentPostProcessorsFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public List<EnvironmentPostProcessor> getEnvironmentPostProcessors(DeferredLogFactory logFactory,
            ConfigurableBootstrapContext bootstrapContext) {
        if (classLoader instanceof SpringClassLoader) {
            return List.of(new DelegatingConfigDataEnvironmentPostProcessor(logFactory, bootstrapContext));
        }
        return EnvironmentPostProcessorsFactory
                .fromSpringFactories(this.classLoader.getParent())
                .getEnvironmentPostProcessors(logFactory, bootstrapContext);
    }

    static class DelegatingConfigDataEnvironmentPostProcessor extends ConfigDataEnvironmentPostProcessor {

        public DelegatingConfigDataEnvironmentPostProcessor(DeferredLogFactory logFactory,
                ConfigurableBootstrapContext bootstrapContext) {
            super(logFactory, bootstrapContext);
        }

        @Override
        public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
            ResourceLoader resourceLoader = application.getResourceLoader();
            if (!(resourceLoader.getClassLoader() instanceof SpringClassLoader springClassLoader)) {
                super.postProcessEnvironment(environment, application);
                return;
            }

            try (JarClassLoader classLoader = new SpringFactoriesClassLoader(springClassLoader)) {
                resourceLoader = new DefaultResourceLoader(classLoader);
                ReflectionUtils.invokeMethod(PROCESS_ENVIRONMENT_METHOD, this, environment,
                        resourceLoader, application.getAdditionalProfiles());
            }
        }
    }

    static class SpringFactoriesClassLoader extends SpringClassLoader {

        private final ArrayEnumeration<URL> enumerations;

        public SpringFactoriesClassLoader(ClassLoader classLoader) {
            super(classLoader.getParent());
            this.enumerations = new ArrayEnumeration<>(List.of(classResource));
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (AUTO_CONFIGURATION_FILE.equals(name) && enumerations != null) {
                return enumerations.asEnumeration();
            }
            return super.getResources(name);
        }

        @Override
        public void close() {
            CLASSLOADER_CACHE.remove(this);
            super.close();
        }
    }

}
