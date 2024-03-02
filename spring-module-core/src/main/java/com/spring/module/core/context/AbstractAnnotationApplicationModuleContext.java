package com.spring.module.core.context;

import com.abm.module.api.SpringModule;
import com.spring.module.core.module.DisposableModule;
import com.spring.module.core.module.ModuleLifecycleProcessor;
import com.spring.module.core.module.ModulePostProcessor;
import com.spring.module.core.utils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.spring.module.tools.utils.Reflections.getField;

/**
 * 抽象通用的模块上下文
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
public abstract class AbstractAnnotationApplicationModuleContext extends AnnotationApplicationContextAdaptor {

    private static final Log LOG = LogFactory.getLog(AbstractAnnotationApplicationModuleContext.class);

    private SpringModule springModule;
    private Class<?> mainApplicationClass;
    private ApplicationContext rootContext;
    private ModuleLifecycleProcessor lifecycleProcessor;
    private List<ModulePostProcessor> modulePostProcessors = new ArrayList<>();

    public AbstractAnnotationApplicationModuleContext() {
    }

    public AbstractAnnotationApplicationModuleContext(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        super.refresh();
        this.loadBeansIfNecessary();
        this.invokeModulePostProcessors(this);
    }

    protected void loadBeansIfNecessary() {
        lifecycleProcessor = this.getBeanProvider(ModuleLifecycleProcessor.class).getIfAvailable();
        if (lifecycleProcessor != null) {
            lifecycleProcessor.startModule();
        }
    }

    /**
     * 注册单例Bean对象
     *
     * @param bean Bean对象
     */
    public void registerSingletonBean(Object bean) {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) this.getBeanFactory();
        BeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(bean.getClass());
        String beanName = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE
                .generateBeanName(beanDefinition, listableBeanFactory);
        listableBeanFactory.registerSingleton(beanName, bean);
    }

    /**
     * 注册单例Bean对象
     *
     * @param beanName Bean的名称
     * @param bean     Bean对象
     */
    public void registerSingletonBean(String beanName, Object bean) {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) getBeanFactory();
        listableBeanFactory.registerSingleton(beanName, bean);
    }

    /**
     * 不支持扫描
     */
    @Override
    public void scan(@NonNull String... basePackages) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void destroyBeans() {
        this.handleDisposableModules();
        super.destroyBeans();
    }

    @Override
    protected void onClose() {
        super.onClose();
        if (lifecycleProcessor != null) {
            lifecycleProcessor.stopModule();
        }
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        if (!EnvironmentPostProcessorApplicationListener.class.equals(listener.getClass())) {
            super.addApplicationListener(listener);
        }
    }

    /**
     * 执行模块前置操作
     *
     * @param applicationContext 模块上下文
     */
    protected void invokeModulePostProcessors(ApplicationContext applicationContext) {
        ConfigurableApplicationContext rootContext = (ConfigurableApplicationContext) getRootApplicationContext(applicationContext);
        for (ModulePostProcessor modulePostProcessor : this.collectModulePostProcessors(rootContext)) {
            try {
                modulePostProcessor.postProcessAfterModule(this);
            } catch (Exception e) {
                LOG.error("模块后置处理器[" + modulePostProcessor + "]执行后置操作异常", e);
            }
        }
    }

    /**
     * 执行模块后置操作
     *
     * @param springModule 模块信息
     */
    protected void invokeModulePostProcessors(SpringModule springModule) {
        ApplicationContext applicationContext = this.getRootApplicationContext();
        for (ModulePostProcessor modulePostProcessor : this.collectModulePostProcessors(applicationContext)) {
            try {
                modulePostProcessor.postProcessBeforeModule(springModule, this);
            } catch (Exception e) {
                LOG.error("模块[" + this + "]后置处理器[" + modulePostProcessor + "]执行前置操作异常", e);
            }
        }
    }

    /**
     * 处理模块销毁
     */
    protected void handleDisposableModules() {
        ApplicationContext applicationContext = this.getRootApplicationContext();
        List<DisposableModule> disposableModules = BeanUtils.sort(applicationContext, DisposableModule.class);
        for (DisposableModule disposableModule : disposableModules) {
            try {
                disposableModule.destroy(this);
            } catch (Exception e) {
                LOG.error("模块[" + this + "]执行销毁操作[" + disposableModule + "]异常", e);
            }
        }
    }

    protected List<ModulePostProcessor> collectModulePostProcessors(ApplicationContext applicationContext) {
        if (CollectionUtils.isEmpty(this.modulePostProcessors)) {
            List<ModulePostProcessor> modulePostProcessors = BeanUtils.sort(applicationContext, ModulePostProcessor.class);
            this.modulePostProcessors.addAll(modulePostProcessors);
        }
        return this.modulePostProcessors;
    }

    public void setSpringModule(SpringModule springModule) {
        this.springModule = springModule;
    }

    public SpringModule getSpringModule() {
        return springModule;
    }

    public void addModulePostProcessor(ModulePostProcessor modulePostProcessor) {
        this.modulePostProcessors.add(modulePostProcessor);
    }

    public List<ModulePostProcessor> getModulePostProcessors() {
        return modulePostProcessors;
    }

    public void setModulePostProcessors(List<ModulePostProcessor> modulePostProcessors) {
        this.modulePostProcessors = modulePostProcessors;
    }

    public void setMainApplicationClass(Class<?> mainApplicationClass) {
        this.mainApplicationClass = mainApplicationClass;
    }

    public Class<?> getMainApplicationClass() {
        return mainApplicationClass;
    }

    /**
     * @see #getRootApplicationContext(ApplicationContext)
     */
    public ApplicationContext getRootApplicationContext() {
        if (this.rootContext == null) {
            this.rootContext = getRootApplicationContext(this);
        }
        return this.rootContext;
    }

    /**
     * 反射获取{@link ClassPathBeanDefinitionScanner}
     *
     * @return {@link ClassPathBeanDefinitionScanner}
     */
    ClassPathBeanDefinitionScanner getScanner() {
        return getField("scanner", this);
    }

    /**
     * 获取主应用的{@link ApplicationContext}
     *
     * @return {@link ApplicationContext}
     */
    public static ApplicationContext getRootApplicationContext(ApplicationContext cur) {
        while (cur.getParent() != null) {
            cur = cur.getParent();
        }
        return cur;
    }

}
