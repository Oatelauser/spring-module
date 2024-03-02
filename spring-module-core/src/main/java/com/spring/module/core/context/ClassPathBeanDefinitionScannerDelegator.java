package com.spring.module.core.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.spring.module.tools.utils.Reflections.getField;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedRepeatableAnnotations;

/**
 * {@link ClassPathBeanDefinitionScanner}代理
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2023-12-31
 * @since 1.0
 */
class ClassPathBeanDefinitionScannerDelegator {

    private final Class<?> applicationClass;
    private final ClassPathBeanDefinitionScanner scanner;

    public ClassPathBeanDefinitionScannerDelegator(ClassPathBeanDefinitionScanner scanner,
            ApplicationContext applicationContext) {
        this.scanner = scanner;
        this.applicationClass = applicationContext.getBean(ApplicationClassHolder.class)
                .getApplicationClass();
    }

    private Set<String> resolveExcludePackages() {
        Set<ComponentScan> annotations = findMergedRepeatableAnnotations(applicationClass, ComponentScan.class);
        if (CollectionUtils.isEmpty(annotations)) {
            return null;
        }

        Set<String> scanPackageNames = new HashSet<>();
        for (ComponentScan annotation : annotations) {
            Class<?>[] basePackageClasses = annotation.basePackageClasses();
            if (!ObjectUtils.isEmpty(basePackageClasses)) {
                for (Class<?> basePackageClass : basePackageClasses) {
                    scanPackageNames.add(basePackageClass.getPackageName());
                }
            }

            String[] basePackages = annotation.basePackages();
            if (!ObjectUtils.isEmpty(basePackages)) {
                scanPackageNames.addAll(Set.of(basePackages));
            }
        }

        if (CollectionUtils.isEmpty(scanPackageNames)) {
            scanPackageNames.add(this.applicationClass.getPackageName());
        }
        return scanPackageNames;
    }

    public void addSubApplicationExcludeFilter() {
        Set<String> excludePackages = this.resolveExcludePackages();
        if (CollectionUtils.isEmpty(excludePackages)) {
            return;
        }

        for (String excludePackage : excludePackages) {
            scanner.addExcludeFilter(new PackageTypeFilter(excludePackage));
        }
    }

    public void extendScanner(ClassPathBeanDefinitionScanner scanner) {
        // autowireCandidatePatterns
        String[] autowireCandidatePatterns = getField("autowireCandidatePatterns", scanner);
        this.scanner.setAutowireCandidatePatterns(autowireCandidatePatterns);
        // beanDefinitionDefaults
        this.scanner.setBeanDefinitionDefaults(scanner.getBeanDefinitionDefaults());
        // includeAnnotationConfig
        boolean includeAnnotationConfig = getField("includeAnnotationConfig", scanner);
        this.scanner.setIncludeAnnotationConfig(includeAnnotationConfig);
        // excludeFilters
        List<TypeFilter> excludeFilters = getField("excludeFilters", scanner);
        if (!CollectionUtils.isEmpty(excludeFilters)) {
            excludeFilters.forEach(this.scanner::addExcludeFilter);
        }
        // includeFilters
        List<TypeFilter> includeFilters = getField("includeFilters", scanner);
        if (!CollectionUtils.isEmpty(includeFilters)) {
            includeFilters.forEach(this.scanner::addIncludeFilter);
        }
    }

    public ClassPathBeanDefinitionScanner getScanner() {
        return scanner;
    }

    static class PackageTypeFilter extends AbstractClassTestingTypeFilter {

        private final String packageName;

        PackageTypeFilter(String packageName) {
            this.packageName = packageName;
        }

        @Override
        protected boolean match(@NonNull ClassMetadata metadata) {
            return metadata.getClassName().startsWith(this.packageName);
        }
    }

}
