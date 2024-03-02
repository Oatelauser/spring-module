package com.spring.module.core.module;

import com.abm.module.api.SpringModule;
import com.abm.module.api.TriadMetadata;
import com.spring.module.core.context.AnnotationApplicationModuleContext;
import com.spring.module.core.loading.*;
import com.spring.module.core.parse.ModuleDependency;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.spring.module.core.exception.ModuleRegistryException.alreadyExistModule;
import static com.spring.module.core.utils.PathUtils.getLibFile;

/**
 * 应用模块注册器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2023-12-29
 * @since 1.0
 */
public class ApplicationModulesRegistrar extends AbstractGraphModulesRegistrar implements BeanClassLoaderAware {

    private static final Log LOG = LogFactory.getLog(ApplicationModulesRegistrar.class);

    private SpecModuleLoader moduleLoader;
    private ClassLoader applicationClassLoader;

    @Override
    public AnnotationApplicationModuleContext installModule(InputStream is) throws IOException {
        ModuleInfo moduleInfo = moduleLoader.loadingSpecModule(is);
        SpringModule springModule = this.loadingModule(moduleInfo);
        String moduleName = springModule.getModuleName();
        if (this.applicationContext(moduleName) != null) {
            alreadyExistModule("模块已经注册: " + moduleName);
        }

        AnnotationApplicationModuleContext applicationContext = this.installSpringModule(springModule);
        LOG.info("模块注册成功: " + moduleName);
        return applicationContext;
    }

    protected AnnotationApplicationModuleContext installSpringModule(SpringModule springModule) {
        Set<TriadMetadata> dependencies = springModule.getDependencies();
        AnnotationApplicationModuleContext applicationContext;
        try {
            applicationContext = CollectionUtils.isEmpty(dependencies) ?
                    this.createApplicationModuleContext(springModule,
                            new DefaultResourceLoader(springModule.getClassLoader()), null) :
                    this.createApplicationModuleContext(springModule);
        } catch (Exception ex) {
            ((JarClassLoader) springModule.getClassLoader()).close();
            throw ex;
        }

        return this.installModuleApplication(applicationContext);
    }

    @Override
    public AnnotationApplicationModuleContext installModuleApplication(AnnotationApplicationModuleContext applicationContext) {
        try {
            return super.installModuleApplication(applicationContext);
        } catch (Exception e) {
            applicationContext.close();
            throw e;
        }
    }

    public AnnotationApplicationModuleContext applicationContext(TriadMetadata metadata) {
        String moduleName = moduleNameResolver.obtainModuleName(metadata);
        return this.applicationContext(moduleName);
    }

    protected SpringModule loadingModule(ModuleInfo moduleInfo) throws IOException {
        File jarFile = moduleInfo.getJarFile();
        SpringModule springModule = new SpringModule(jarFile);
        this.loadingModuleInfo(moduleInfo, springModule);
        this.loadingClassLoader(this.applicationClassLoader, moduleInfo, springModule);
        this.loadingModuleName(springModule);
        // 必须指定父类平台类加载器，防止双亲委派的影响
        ClassLoader platformClassLoader = this.applicationClassLoader.getParent();
        try (JarClassLoader tempClassLoader = JarClassLoader.load(jarFile, platformClassLoader)) {
            this.loadingSpringFactories(tempClassLoader, (SpringClassLoader) springModule.getClassLoader());
        }
        return springModule;
    }

    @SuppressWarnings("all")
    protected void loadingSpringFactories(ClassLoader tempClassLoader, SpringClassLoader classLoader) throws IOException {
        Map<String, ArrayEnumeration<URL>> loadResources = new HashMap<>();
        for (String resourceName : SpringClassLoader.SPRING_SPI_RESOURCES) {
            Enumeration<URL> resources = tempClassLoader.getResources(resourceName);
            ArrayEnumeration<URL> dataResults = new ArrayEnumeration<>(resources);
            loadResources.put(resourceName, dataResults);
        }
        classLoader.setLocationResources(loadResources);
    }

    protected void loadingClassLoader(ClassLoader parent, ModuleInfo moduleInfo, SpringModule springModule) throws IOException {
        JarClassLoader moduleClassLoader = new SpringClassLoader(parent);
        moduleClassLoader.addJar(moduleInfo.getJarFile());
        File libFile = getLibFile(moduleInfo.getJarFile());
        for (Path lib : moduleInfo.getIncludeLibs()) {
            moduleClassLoader.addJar(new File(libFile, lib.toString()));
        }
        springModule.setClassLoader(moduleClassLoader);
    }

    protected void loadingModuleInfo(ModuleInfo moduleInfo, SpringModule springModule) {
        ModuleDependency moduleDependency = moduleInfo.getModuleDependency();
        springModule.setProjectMetadata(moduleDependency.getProjectMetadata());
        Set<TriadMetadata> dependencies = moduleDependency.getDependencies();
        springModule.setDependencies(dependencies);
        springModule.setParentModules(moduleDependency.getParentModules());
    }

    protected void loadingModuleName(SpringModule springModule) {
        String moduleName = moduleNameResolver.obtainModuleName(springModule.getProjectMetadata());
        springModule.setModuleName(moduleName);
    }

    @Override
    public void afterSingletonsInstantiated() {
        super.afterSingletonsInstantiated();
        this.moduleLoader = applicationContext.getBean(SpecModuleLoader.class);
    }

    @Override
    public void setBeanClassLoader(@NonNull ClassLoader classLoader) {
        this.applicationClassLoader = classLoader;
    }

}
