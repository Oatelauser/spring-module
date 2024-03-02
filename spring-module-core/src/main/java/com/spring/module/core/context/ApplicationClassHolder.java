package com.spring.module.core.context;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.io.File;

import static com.spring.module.tools.utils.JarFilePaths.resolveJarPath;

/**
 * 获取Spring应用主类
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-07
 * @since 1.0
 */
public class ApplicationClassHolder {

    private final File jarFile;
    private final String beanName;
    private final Class<?> applicationClass;

    public ApplicationClassHolder(ApplicationContext applicationContext) {
        this.beanName = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class)[0];
        this.applicationClass = ClassUtils.getUserClass(applicationContext.getBean(beanName));
        this.jarFile = resolveJarPath(this.getClass());
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    public File getJarFile() {
        return jarFile;
    }

}
