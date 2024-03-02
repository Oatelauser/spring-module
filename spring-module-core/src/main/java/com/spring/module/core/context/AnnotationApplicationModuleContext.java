package com.spring.module.core.context;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScopeMetadataResolver;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.spring.module.tools.utils.Reflections.getField;


/**
 * Spring模块化的应用上下文
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-02
 * @see AnnotationConfigApplicationContext
 * @since 1.0
 */
public class AnnotationApplicationModuleContext extends AbstractAnnotationApplicationModuleContext {

    /**
     * 是否初始化上下文
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    //public AnnotationApplicationModuleContext() {
    //    super(new MyListableBeanFactory());
    //}

    /**
     * 初始化模块的应用上下文
     *
     * @param applicationContext 应用上下文
     */
    protected void initializeModuleApplication(ApplicationContext applicationContext) {
        if (this.initialized.get()) {
            return;
        }

        applicationContext = getRootApplicationContext(applicationContext);
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        this.setAllowBeanDefinitionOverriding(listableBeanFactory.isAllowBeanDefinitionOverriding());
        this.setAllowCircularReferences(listableBeanFactory.isAllowCircularReferences());

        // ClassPathBeanDefinitionScanner
        ClassPathBeanDefinitionScanner currentScanner = this.getScanner();
        ClassPathBeanDefinitionScannerDelegator sc = new ClassPathBeanDefinitionScannerDelegator(currentScanner, applicationContext);

        // AnnotationConfigServletWebServerApplicationContext
        if (configurableApplicationContext instanceof AnnotationConfigServletWebServerApplicationContext servletApplicationContext) {
            this.initializeAnnotationConfigServletWebServerApplicationContext(sc, currentScanner, servletApplicationContext);
        } else {
            throw new UnsupportedOperationException("不支持应用上下文的类型：" + configurableApplicationContext);
        }
        sc.addSubApplicationExcludeFilter();

        this.initialized.set(true);
    }

    /**
     * 基于SpringMVC的应用上下文初始化模块上下文
     *
     * @param sc                        {@link ClassPathBeanDefinitionScannerDelegator}
     * @param currentScanner            {@link ClassPathBeanDefinitionScanner}
     * @param servletApplicationContext {@link AnnotationConfigServletWebServerApplicationContext}
     */
    private void initializeAnnotationConfigServletWebServerApplicationContext(ClassPathBeanDefinitionScannerDelegator sc,
            ClassPathBeanDefinitionScanner currentScanner, AnnotationConfigServletWebServerApplicationContext servletApplicationContext) {
        // 设置ScopeMetadataResolver
        ClassPathBeanDefinitionScanner scanner = getField("scanner", servletApplicationContext);
        ScopeMetadataResolver scopeMetadataResolver = getField("scopeMetadataResolver", scanner);
        this.setScopeMetadataResolver(scopeMetadataResolver);

        //BeanNameGenerator beanNameGenerator = getField("beanNameGenerator", scanner);
        //this.setBeanNameGenerator(beanNameGenerator);

        // 通过全类名作为bean的名称，防止bean名称冲突
        this.setBeanNameGenerator(FullyQualifiedAnnotationBeanNameGenerator.INSTANCE);

        // 初始化ClassPathBeanDefinitionScanner
        sc.extendScanner(scanner);
    }

}
