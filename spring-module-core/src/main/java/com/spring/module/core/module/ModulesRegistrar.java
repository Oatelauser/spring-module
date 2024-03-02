package com.spring.module.core.module;

import com.spring.module.core.context.AnnotationApplicationModuleContext;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * 应用模块注册器接口
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
public interface ModulesRegistrar {

    /**
     * 通过模块包的输入流加载模块
     *
     * @param is 模块包输入流
     * @return 模块应用上下文
     * @throws IOException 加载模块的IO异常
     */
    AnnotationApplicationModuleContext installModule(InputStream is) throws IOException;

    /**
     * @see #installModule(InputStream)
     */
    default AnnotationApplicationModuleContext installModule(File jarFile) throws IOException {
        Assert.isTrue(jarFile.exists(), () -> "不存在的JAR路径：" + jarFile);
        return this.installModule(new FileInputStream(jarFile));
    }

    /**
     * @see #installModule(File)
     */
    default AnnotationApplicationModuleContext installModule(String jarPath) throws IOException {
        return this.installModule(new File(jarPath));
    }

    /**
     * 注册模块应用上下文
     *
     * @param applicationContext 模块上下文
     */
    AnnotationApplicationModuleContext installModuleApplication(AnnotationApplicationModuleContext applicationContext);

    /**
     * 查找模块上下文
     *
     * @param moduleName 模块名
     * @return {@link AnnotationApplicationModuleContext}
     */
    @Nullable
    AnnotationApplicationModuleContext applicationContext(String moduleName);

    /**
     * 卸载模块
     *
     * @param moduleName 模块名
     */
    void uninstallModule(String moduleName);

    /**
     * @see #uninstallModule(String)
     */
    default void uninstallModule(AnnotationApplicationModuleContext applicationModuleContext) {
        this.uninstallModule(applicationModuleContext.getId());
    }

    /**
     * 获取模块依赖关系
     *
     * @param strategy 依赖的策略
     * @return 关系图
     */
    Graph<String, DefaultEdge> obtainModuleDependencies(DependencyStrategy strategy);

    default Graph<String, DefaultEdge> obtainModuleDependencies() {
        return obtainModuleDependencies(DependencyStrategy.ASC);
    }

    /**
     * 查询所有注册的模块名
     *
     * @return 所有的模块名
     */
    Set<String> getAllModuleNames();

    enum DependencyStrategy {
        /**
         * 顺序的依赖关系：source <- target
         */
        ASC,

        /**
         * 倒序的依赖关系：target <- source
         */
        DESC,

    }

}
