package com.spring.module.core.parse;

import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URL;

import static com.spring.module.core.utils.PathUtils.DEPENDENCY_NAME;

/**
 * JAR包依赖解析器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public interface DependencyAnalyzer {

    String LOCATION_RESOURCE = "META-INF/" + DEPENDENCY_NAME;

    /**
     * 解析JAR包依赖
     *
     * @param resource 依赖文件资源
     * @return 模块依赖信息
     * @throws IOException 解析IO异常
     */
    ModuleDependency analyzeDependencies(URL resource) throws IOException;

    /**
     * 分析JAR包的所有依赖
     *
     * @param classLoader 模块的类加器
     * @throws IOException IO异常
     */
    default ModuleDependency analyzeDependencies(ClassLoader classLoader) throws IOException {
        URL resource = classLoader.getResource(LOCATION_RESOURCE);
        Assert.notNull(resource, "JAR包不存在[" + LOCATION_RESOURCE + "]文件");
        return this.analyzeDependencies(resource);
    }

}
