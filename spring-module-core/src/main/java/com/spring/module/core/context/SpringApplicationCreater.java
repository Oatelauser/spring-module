package com.spring.module.core.context;

import com.abm.module.api.TriadMetadata;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * 自动生成模块的应用上下文类
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
@SuppressWarnings("SpellCheckingInspection")
public class SpringApplicationCreater {

    public Class<?> createVirtualApplication(TriadMetadata projectMetadata, ClassLoader classLoader) {
        DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .subclass(Object.class)
                .name(this.generateClassName(projectMetadata))
                .annotateType(this.createAnnotation(classLoader))
                .make();
        try (unloaded) {
            return unloaded.load(classLoader).getLoaded();
        }
    }

    protected String generateClassName(TriadMetadata projectMetadata) {
        return  projectMetadata.groupId() + "."
                + projectMetadata.artifactId().replace("-", "")
                + ".SpringModuleApplication";
    }

    protected AnnotationDescription createAnnotation(ClassLoader classLoader) {
        //SpringModuleLoader loader = new SpringModuleLoader(classLoader);
        //Map<String, List<String>> factories = loader.getFactories();
        Set<String> excludeClasses = new HashSet<>();
        //if (!CollectionUtils.isEmpty(factories)) {
        //    excludeClasses = Set.copyOf(factories.get(getClass().getName()));
        //}

        AnnotationDescription.Builder annotation = AnnotationDescription.Builder
                .ofType(SpringBootApplication.class);
        annotation.defineArray("excludeName", excludeClasses.toArray(new String[0]));
        annotation.define("nameGenerator", FullyQualifiedAnnotationBeanNameGenerator.class);
        annotation.define("proxyBeanMethods", false);
        return annotation.build();
    }

}
