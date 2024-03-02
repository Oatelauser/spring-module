package com.spring.module.core.utils;

import java.io.File;
import java.nio.file.Path;

/**
 * 路径工具类
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-16
 * @since 1.0
 */
public class PathUtils {

    public static final String LIB_NAME = "lib";
    public static final String JAR_NAME = ".jar";
    public static final String DEPENDENCY_NAME = "dependencies.dot";


    public static Path subpath(Path path, Path prepath) {
        if (!path.startsWith(prepath)) {
            return path;
        }
        return path.subpath(prepath.getNameCount(), path.getNameCount());
    }

    public static Path createRandomSubpath(Path path) {
        return path.resolve(NanoIdUtils.randomNanoId(8));
    }

    /**
     * 通过模块JAR包获取相对的lib目录
     *
     * @param jarFile 模块JAR包
     * @return lib目录的路径
     */
    public static File getLibFile(File jarFile) {
        return new File(jarFile.getParent(), LIB_NAME);
    }

}
