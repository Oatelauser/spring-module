package com.spring.module.core.loading;

import com.abm.module.api.TriadMetadata;
import com.spring.module.core.autoconfigure.ModuleProperties;
import com.spring.module.core.context.AnnotationApplicationModuleContext;
import com.spring.module.core.module.ModulesRegistrar;
import com.spring.module.core.parse.DependencyAnalyzer;
import com.spring.module.core.parse.ModuleDependency;
import com.spring.module.core.utils.ZipUtil;
import com.spring.module.tools.utils.JarFilePaths;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.spring.module.core.exception.ModuleLoadException.invalidModuleFile;
import static com.spring.module.core.parse.DependencyAnalyzer.LOCATION_RESOURCE;
import static com.spring.module.core.utils.PathUtils.*;

/**
 * 加载标准模块包
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-10
 * @since 1.0
 */
public class SpringModuleLoader implements SpecModuleLoader, ApplicationContextAware,
        SmartInitializingSingleton, ApplicationRunner {

    private ModulesRegistrar modulesRegistrar;
    private DependencyAnalyzer dependencyAnalyzer;
    private ApplicationContext applicationContext;

    private ModuleDependency moduleDependency;
    private final ModuleProperties moduleProperties;

    public SpringModuleLoader(ModuleProperties moduleProperties) {
        this.moduleProperties = moduleProperties;
    }

    @Override
    public ModuleInfo loadingSpecModule(InputStream is) throws IOException {
        Path destPath = Paths.get(moduleProperties.getPackagesPath());
        //destPath = createRandomSubpath(destPath);
        Set<Path> paths = ZipUtil.unzip(is, destPath);
        ModuleInfo moduleInfo = new ModuleInfo();
        Set<Path> rootPaths = this.resolveModuleRootPaths(destPath, paths);
        this.validateModuleFile(rootPaths, paths, moduleInfo);
        this.loadingDependencies(moduleInfo);
        return moduleInfo;
    }

    //protected ModuleInfo loadingSpecModule(Path destPath, InputStream is) throws IOException {
    //    Set<Path> paths = ZipUtil.unzip(is, destPath);
    //    ModuleInfo moduleInfo = new ModuleInfo();
    //    Set<Path> rootPaths = this.resolveModuleRootPaths(destPath, paths);
    //    this.validateModuleFile(rootPaths, paths, moduleInfo);
    //    this.loadingDependencies(moduleInfo);
    //    return moduleInfo;
    //}

    @SuppressWarnings("ConstantConditions")
    protected void loadingDependencies(ModuleInfo moduleInfo) throws IOException {
        URL resource = moduleInfo.getDependencyFile().toURI().toURL();
        ModuleDependency moduleDependency = dependencyAnalyzer.analyzeDependencies(resource);
        moduleInfo.setModuleDependency(moduleDependency);

        Set<TriadMetadata> dependencies = moduleDependency.copyExcludeSelfDependencies();
        List<String> parentModules = moduleDependency.getParentModules();
        if (!CollectionUtils.isEmpty(parentModules)) {
            for (String parentModule : parentModules) {
                AnnotationApplicationModuleContext parentContext = modulesRegistrar.applicationContext(parentModule);
                Set<TriadMetadata> exclude = parentContext.getSpringModule().getDependencies();
                if (!CollectionUtils.isEmpty(dependencies) && !CollectionUtils.isEmpty(exclude)) {
                    dependencies.removeAll(exclude);
                }
            }
        }
        this.loadingDependencies(dependencies, moduleInfo);
    }

    protected void loadingDependencies(Set<TriadMetadata> dependencies, ModuleInfo moduleInfo) {
        Set<TriadMetadata> parentDependencies = this.moduleDependency.getDependencies();
        if (!CollectionUtils.isEmpty(parentDependencies) && !CollectionUtils.isEmpty(dependencies)) {
            dependencies.removeAll(parentDependencies);
        }

        List<Path> libs = moduleInfo.getIncludeLibs();
        if (CollectionUtils.isEmpty(dependencies)) {
            moduleInfo.setIncludeLibs(List.of());
            moduleInfo.setExcludeLibs(libs);
            return;
        }

        List<Path> dependencyFiles = new ArrayList<>();
        for (TriadMetadata dependency : dependencies) {
            String fileName = dependency.getJarFileName();
            dependencyFiles.add(Path.of(fileName));
        }
        libs.removeAll(dependencyFiles);
        moduleInfo.setIncludeLibs(dependencyFiles);
        moduleInfo.setExcludeLibs(libs);
    }

    @SuppressWarnings("ConstantConditions")
    protected Set<Path> resolveModuleRootPaths(Path destModulePath, Set<Path> paths) {
        Set<Path> pathSet = paths.stream().map(path -> subpath(path, destModulePath))
                .map(path -> path.getName(0)).collect(Collectors.toSet());
        return pathSet.stream().map(destModulePath::resolve)
                .collect(Collectors.toSet());
    }

    /**
     * 校验模块文件是否符合规范
     *
     * @param rootPaths  模块文件包含的所有根目录
     * @param paths      模块文件中所有的路径
     * @param moduleInfo 模块信息
     */
    protected void validateModuleFile(Set<Path> rootPaths, Set<Path> paths, ModuleInfo moduleInfo) {
        if (CollectionUtils.isEmpty(rootPaths)) {
            invalidModuleFile("上传的模块文件没有任何文件");
        }
        if (rootPaths.size() > 1) {
            invalidModuleFile("上传的模块文件必须存在一个根目录");
        }
        Path rootPath = Objects.requireNonNull(CollectionUtils.firstElement(rootPaths));
        if (!Files.isDirectory(rootPath)) {
            invalidModuleFile("上传的模块文件不符合规范，必须是一个文件夹");
        }

        Path jarPath = rootPath.resolve(rootPath.getFileName() + JAR_NAME);
        if (Files.notExists(jarPath)) {
            invalidModuleFile("上传的模块文件根目录不存在模块JAR包[" + jarPath + "]");
        }
        moduleInfo.setJarFile(jarPath.toFile());

        Path lib = rootPath.resolve(LIB_NAME);
        if (!paths.contains(lib)) {
            invalidModuleFile("上传的模块文件不存在lib目录");
        }
        List<Path> libFiles = paths.stream().filter(path -> path.startsWith(lib) && !path.equals(lib))
                .map(Path::getFileName).toList();
        moduleInfo.setIncludeLibs(new ArrayList<>(libFiles));

        Path dependencies = rootPath.resolve(DEPENDENCY_NAME);
        if (Files.notExists(dependencies)) {
            invalidModuleFile("上传的模块文件不存在文件[" + DEPENDENCY_NAME + "]");
        }
        moduleInfo.setDependencyFile(dependencies.toFile());
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (this.modulesRegistrar == null) {
            this.modulesRegistrar = this.applicationContext.getBean(ModulesRegistrar.class);
        }
        if (this.dependencyAnalyzer == null) {
            this.dependencyAnalyzer = this.applicationContext.getBean(DependencyAnalyzer.class);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        File rootFile = JarFilePaths.resolveJarPath(getClass());
        URL resource = new File(rootFile, LOCATION_RESOURCE).toURI().toURL();
        this.moduleDependency = dependencyAnalyzer.analyzeDependencies(resource);
    }

    public void setModulesRegistrar(ModulesRegistrar modulesRegistrar) {
        this.modulesRegistrar = modulesRegistrar;
    }

    public void setDependencyAnalyzer(DependencyAnalyzer dependencyAnalyzer) {
        this.dependencyAnalyzer = dependencyAnalyzer;
    }

}
