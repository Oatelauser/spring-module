package com.spring.module.core.process;

import com.spring.module.tools.utils.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ConfigurationClassUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.*;

import java.util.*;

import static com.spring.module.core.context.ApplicationBootstrapRegistry.getBootstrapFilteringBeanNames;
import static com.spring.module.core.context.ApplicationBootstrapRegistry.resolveBeanClass;

/**
 * 重新定义Bean的名称防止模块之间的冲突，同时兼容Bean的源名称
 * <p>
 * 1.优化@Bean注解Bean的名称 = 所在配置类名 + 源Bean名称
 * 2.优化配置类的Bean名称 = 所在配置类名
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-09
 * @since 1.0
 */
public class BeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * 用于判断是配置类Bean的标识：@Configuration、@AutoConfiguration、@Import等
     */
    public static final String CONFIGURATION_CLASS_ATTRIBUTE;

    /**
     * 用于判断是@Bean注解标识的Bean对象
     */
    public static final Class<?> ANNOTATED_BEAN_DEFINITION_CLASS;

    /**
     * 设置Bean对象特殊标签
     */
    public static final String REDEFINE_ATTRIBUTE_NAME = BeanDefinitionRegistry.class.getName();

    static {
        ANNOTATED_BEAN_DEFINITION_CLASS = ClassUtils.resolveClassName("org.springframework.context" +
                ".annotation.ConfigurationClassBeanDefinitionReader$ConfigurationClassBeanDefinition", null);
        CONFIGURATION_CLASS_ATTRIBUTE = Reflections.getField("CONFIGURATION_CLASS_ATTRIBUTE", ConfigurationClassUtils.class);
    }

    private final AnnotationConfigApplicationContext applicationContext;
    final List<BeanDefinition> configBeans = new ArrayList<>();

    public BeanRegistryPostProcessor(ApplicationContext applicationContext) {
        this.applicationContext = (AnnotationConfigApplicationContext) applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        String applicationId = Objects.requireNonNull(this.applicationContext.getId());
        Set<String> filteringBeanNames = getBootstrapFilteringBeanNames(applicationContext);
        BeanParameter beanParameter = new BeanParameter();

        for (String beanName : registry.getBeanDefinitionNames()) {
            if (filteringBeanNames.contains(beanName)) {
                continue;
            }
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.hasAttribute(applicationId)) {
                continue;
            }
            Class<?> beanClass = this.filteringBeanDefinition(beanName, beanDefinition);
            if (beanClass == null) {
                continue;
            }
            beanParameter.beanName = beanName;
            beanParameter.beanClass = beanClass;
            beanParameter.beanDefinition = beanDefinition;
            this.postProcessBeanDefinitionRegistry(beanParameter, registry);
        }

        this.postProcessBeanDefinitionRegistry(beanParameter);
    }

    private void postProcessBeanDefinitionRegistry(BeanParameter definitionHolder) {
        if (CollectionUtils.isEmpty(definitionHolder.candidateBeans)
                || CollectionUtils.isEmpty(definitionHolder.candidateConfigs)) {
            return;
        }

        for (Map.Entry<String, List<BeanDefinition>> entry : definitionHolder.candidateBeans.entrySet()) {
            String redefineConfigBeanName = definitionHolder.candidateConfigs.get(entry.getKey());
            List<BeanDefinition> beanDefinitions = entry.getValue();
            if (StringUtils.hasText(redefineConfigBeanName) && !CollectionUtils.isEmpty(beanDefinitions)) {
                for (BeanDefinition beanDefinition : beanDefinitions) {
                    beanDefinition.setFactoryBeanName(redefineConfigBeanName);
                }
            }
        }
    }

    protected void postProcessBeanDefinitionRegistry(BeanParameter beanParameter, BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = beanParameter.beanDefinition;
        if (beanDefinition.hasAttribute(CONFIGURATION_CLASS_ATTRIBUTE)) {
            this.postProcessClassBeanDefinitionRegistry(beanParameter, registry);
            return;
        }
        if (ANNOTATED_BEAN_DEFINITION_CLASS.equals(beanDefinition.getClass())) {
            this.postProcessAnnotatedBeanDefinitionRegistry(beanParameter, registry);
            return;
        }
        throw new RuntimeException("不支持的BeanDefinition：" + beanDefinition);
    }

    @SuppressWarnings("ConstantConditions")
    protected void postProcessAnnotatedBeanDefinitionRegistry(BeanParameter beanParameter,
            BeanDefinitionRegistry registry) {
        String beanName = beanParameter.beanName;
        registry.removeBeanDefinition(beanName);
        BeanDefinition beanDefinition = beanParameter.beanDefinition;
        String configurationClassName = beanDefinition.getFactoryBeanName();
        String redefineBeanName = configurationClassName + "#" + beanName;
        this.redefineBeanDefinition(beanName, redefineBeanName, beanDefinition, registry);
        beanParameter.candidateBeans.add(configurationClassName, beanDefinition);
        configBeans.add(beanDefinition);
    }

    private void postProcessClassBeanDefinitionRegistry(BeanParameter beanParameter,
            BeanDefinitionRegistry registry) {
        String beanName = beanParameter.beanName;
        String redefineBeanName = beanParameter.beanClass.getName();
        if (beanName.equals(redefineBeanName)) {
            return;
        }

        registry.removeBeanDefinition(beanName);
        BeanDefinition beanDefinition = beanParameter.beanDefinition;
        this.redefineBeanDefinition(beanName, redefineBeanName, beanDefinition, registry);
        beanParameter.candidateConfigs.put(beanName, redefineBeanName);
    }

    private void redefineBeanDefinition(String beanName, String redefineBeanName,
            BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        beanDefinition.setAttribute(REDEFINE_ATTRIBUTE_NAME, beanName);
        beanDefinition.setAttribute("value", beanName);
        registry.registerBeanDefinition(redefineBeanName, beanDefinition);
    }

    protected Class<?> filteringBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = null;
        try {
            beanClass = resolveBeanClass(beanName, beanDefinition, this.applicationContext);
        } catch (ClassNotFoundException ignored) {
        }
        if (beanClass == null) {
            return null;
        }
        beanClass = ClassUtils.getUserClass(beanClass);
        if (!beanClass.getClassLoader().equals(applicationContext.getClassLoader())) {
            return null;
        }
        return beanClass;
    }

    protected static class BeanParameter {
        String beanName;
        Class<?> beanClass;
        BeanDefinition beanDefinition;
        MultiValueMap<String, BeanDefinition> candidateBeans = new LinkedMultiValueMap<>();
        Map<String, String> candidateConfigs = new HashMap<>();
    }

}
