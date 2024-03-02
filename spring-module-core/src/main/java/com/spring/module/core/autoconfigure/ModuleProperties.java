package com.spring.module.core.autoconfigure;

import com.spring.module.tools.utils.JarFilePaths;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 模块应用的配置
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-16
 * @since 1.0
 */
@ConfigurationProperties(prefix = "spring.module")
public class ModuleProperties implements InitializingBean {

    /**
     * 存放模块包的路径，默认是JAR包同级目录"module"
     */
    private String packagesPath = "E:\\spring-module";

    public String getPackagesPath() {
        return packagesPath;
    }

    public void setPackagesPath(String packagesPath) {
        this.packagesPath = packagesPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Path path = Paths.get(packagesPath);
        File jarPath = JarFilePaths.resolveJarPath(getClass());
        if (!path.isAbsolute()) {
            path = jarPath.toPath().resolve(packagesPath);
            this.packagesPath = path.toString();
        }
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}
