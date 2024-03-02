package com.spring.module.core.loading;

import com.spring.module.core.parse.ModuleDependency;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * 加载的模块信息
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-16
 * @since 1.0
 */
public class ModuleInfo {

    /**
     * 模块JAR包文件路径，文件名应该和模块zip包的根路径一致
     */
    private File jarFile;
    /**
     * 模块包含的私有依赖
     */
    private List<Path> includeLibs = List.of();

    /**
     * 父模块已经存在的依赖当前模块就不需要加载
     */
    private List<Path> excludeLibs = List.of();

    /**
     * 依赖项dependencies.dot文件路径
     */
    private File dependencyFile;

    /**
     * 解析{@link #dependencyFile}之后的结果信息
     */
    private ModuleDependency moduleDependency;

    public File getJarFile() {
        return jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public List<Path> getIncludeLibs() {
        return includeLibs;
    }

    public void setIncludeLibs(List<Path> includeLibs) {
        this.includeLibs = includeLibs;
    }

    public List<Path> getExcludeLibs() {
        return excludeLibs;
    }

    public void setExcludeLibs(List<Path> excludeLibs) {
        this.excludeLibs = excludeLibs;
    }

    public File getDependencyFile() {
        return dependencyFile;
    }

    public void setDependencyFile(File dependencyFile) {
        this.dependencyFile = dependencyFile;
    }

    public void setModuleDependency(ModuleDependency moduleDependency) {
        this.moduleDependency = moduleDependency;
    }

    public ModuleDependency getModuleDependency() {
        return moduleDependency;
    }

}
