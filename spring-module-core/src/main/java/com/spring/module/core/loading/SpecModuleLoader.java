package com.spring.module.core.loading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 模块加载器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-15
 * @since 1.0
 */
public interface SpecModuleLoader {

    /**
     * 加载并校验模块包的基础信息
     *
     * @param is 模块包的输入流
     * @return 加载的模块信息
     * @throws IOException 加载IO异常
     */
    ModuleInfo loadingSpecModule(InputStream is) throws IOException;

    /**
     * 加载并校验模块包的基础信息
     *
     * @param srcFile 模块包的路径
     * @return 加载的模块信息
     * @throws IOException 加载IO异常
     */
    default ModuleInfo loadingSpecModule(File srcFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(srcFile)) {
            return loadingSpecModule(fis);
        }
    }

}
