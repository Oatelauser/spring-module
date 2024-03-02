package com.abm.module.api;


import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * 模块信息描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
@SuppressWarnings("unused")
public class SpringModule {

    private String moduleName;
    private final File jarFile;
    private ClassLoader classLoader;

    private List<String> parentModules;
    private TriadMetadata projectMetadata;
    private Set<TriadMetadata> dependencies;

    public SpringModule(File jarFile) {
        this.jarFile = jarFile;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TriadMetadata getProjectMetadata() {
        return projectMetadata;
    }

    public void setProjectMetadata(TriadMetadata projectMetadata) {
        this.projectMetadata = projectMetadata;
    }

    public Set<TriadMetadata> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<TriadMetadata> dependencies) {
        this.dependencies = dependencies;
    }

    public File getJarFile() {
        return jarFile;
    }

    public List<String> getParentModules() {
        return parentModules;
    }

    public void setParentModules(List<String> parentModules) {
        this.parentModules = parentModules;
    }

}
