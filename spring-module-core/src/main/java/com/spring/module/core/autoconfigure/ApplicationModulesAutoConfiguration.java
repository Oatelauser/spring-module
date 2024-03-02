package com.spring.module.core.autoconfigure;

import com.spring.module.core.context.*;
import com.spring.module.core.loading.SpecModuleLoader;
import com.spring.module.core.loading.SpringModuleLoader;
import com.spring.module.core.module.ApplicationModulesRegistrar;
import com.spring.module.core.module.ModulesRegistrar;
import com.spring.module.core.parse.DependencyAnalyzer;
import com.spring.module.core.parse.MavenDependencyAnalyzer;
import com.spring.module.core.parse.MavenModuleNameResolver;
import com.spring.module.core.parse.ModuleNameResolver;
import com.spring.module.core.process.ControllerModulePostProcessor;
import com.spring.module.core.process.DestructionModulePostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Spring模块化自动配置类
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2023-12-29
 * @since 1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(ModuleProperties.class)
public class ApplicationModulesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApplicationClassHolder applicationClassHolder(ApplicationContext applicationContext) {
        return new ApplicationClassHolder(applicationContext);
    }

    @Configuration(proxyBeanMethods = false)
    @Import(WebMvcConfigurerRegistrations.class)
    public static class WebMvcConfigurer {
    }

    @Configuration(proxyBeanMethods = false)
    public static class DependencyAnalyzerConfiguration {

        @Bean
        @ConditionalOnMissingBean(DependencyAnalyzer.class)
        public MavenDependencyAnalyzer mavenDependencyAnalyzer() {
            return new MavenDependencyAnalyzer();
        }

        @Bean
        @ConditionalOnBean(MavenDependencyAnalyzer.class)
        @ConditionalOnMissingBean(ModuleNameResolver.class)
        public MavenModuleNameResolver mavenModuleNameResolver() {
            return new MavenModuleNameResolver();
        }

    }

    @Configuration(proxyBeanMethods = false)
    public static class ModuleProcessorConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ControllerModulePostProcessor controllerModulePostProcessor(
                @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping controllerHandlerMapping) {
            return new ControllerModulePostProcessor((ControllerHandlerMapping) controllerHandlerMapping);
        }

    }

    @Configuration(proxyBeanMethods = false)
    public static class ApplicationModuleConfiguration {

        @Bean
        @ConditionalOnMissingBean(SpecModuleLoader.class)
        public SpringModuleLoader springModuleLoader(ModuleProperties moduleProperties) {
            return new SpringModuleLoader(moduleProperties);
        }

        @Bean
        public ApplicationAvailabilityBeans applicationAvailabilityBeans() {
            return new ApplicationAvailabilityBeans();
        }

        @Bean
        public DestructionModulePostProcessor springFactoriesDestroy() {
            return new DestructionModulePostProcessor();
        }

        @Bean
        @ConditionalOnMissingBean
        public ApplicationModuleInitializer applicationContextInitializer() {
            return new ApplicationModuleInitializer();
        }

        @Bean
        @ConditionalOnMissingBean(ApplicationContextFactory.class)
        public ApplicationModuleApplicationContextFactory applicationModuleApplicationContextFactory() {
            return new ApplicationModuleApplicationContextFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public SpringApplicationCreater springApplicationCreater() {
            return new SpringApplicationCreater();
        }

        @Bean
        @ConditionalOnMissingBean(ModulesRegistrar.class)
        public ApplicationModulesRegistrar applicationModulesRegistrar() {
            return new ApplicationModulesRegistrar();
        }

    }

}
