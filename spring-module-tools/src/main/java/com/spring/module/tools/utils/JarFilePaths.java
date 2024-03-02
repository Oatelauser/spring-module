package com.spring.module.tools.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * JAR包路径解析工具
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
public abstract class JarFilePaths {

    /**
     * 通过传入的JAR里面的class对象解析JAR路径
     *
     * @param jarClass JAR包里面的class
     * @return JAR包所在的路径
     */
    public static File resolveJarPath(Class<?> jarClass) {
        // 1. com/test/config/AgentPackagePath.class
        String classPath = jarClass.getName().replaceAll("\\.", "/") + ".class";

        // 2. jar:file:to/path/XXX.jar!com/test/config/AgentPackagePath.class
        URL resource = ClassLoader.getSystemResource(classPath);
        if (resource != null) {
            String urlString = resource.toString();
            int insidePathIndex = urlString.indexOf("!");
            if (insidePathIndex > -1) {
                // 3. file:to/path/XXX.jar
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                File jarFile;
                jarFile = new File(URI.create(urlString));
                if (jarFile.exists()) {
                    // 4. file:to/path
                    return jarFile.getParentFile();
                }
            } else {
                // file:to/path/com/test/config/AgentPackagePath.class
                // file:to/path
                String classLocation = urlString.substring("file:".length(), urlString.length() - classPath.length());
                return new File(classLocation);
            }
        }

        throw new InternalError("Can not locate agent jar file.");
    }

}
