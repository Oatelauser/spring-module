package com.spring.module.core.loading;

import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于JAR包的类加载器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2023-12-31
 * @since 1.0
 */
public class JarClassLoader extends URLClassLoader {

    private final Set<File> loadJarFiles = new HashSet<>();

    // 开启并行加载
    static {
        registerAsParallelCapable();
    }

    /**
     * 传入空的URL，具体资源通过addURL传入
     *
     * @param parent 父类类加载器
     */
    public JarClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addURL(File file) throws MalformedURLException {
        this.addURL(file.toURI().toURL());
        loadJarFiles.add(file);
    }

    /**
     * 添加jar包、指定目录及其子目录下的所有jar包，深度优先策略遍历目录查找jar包
     *
     * @param jarFileOrDir JAR包或者JAR包所在路径
     * @throws IOException IO异常
     */
    public void addJar(File jarFileOrDir) throws IOException {
        if (isJarFile(jarFileOrDir)) {
            this.addURL(jarFileOrDir);
            return;
        }

        File[] files = jarFileOrDir.listFiles();
        if (!ObjectUtils.isEmpty(files)) {
            for (File file : files) {
                this.addJar(file);
            }
        }
    }

    public Set<File> getLoadJarFiles() {
        return Set.copyOf(loadJarFiles);
    }

    public static boolean isJarFile(URI uri) {
        if (uri == null) {
            return false;
        }
        return isJarPath(uri.getPath());
    }

    public static boolean isJarFile(File jarFile) {
        if (jarFile == null) {
            return false;
        }
        return jarFile.isFile() && isJarPath(jarFile.getPath());
    }

    public static boolean isJarPath(String path) {
        return path.endsWith(".jar");
    }

    public static JarClassLoader load(File jarFileOrDir, ClassLoader parent) throws IOException {
        List<File> files = Collections.singletonList(jarFileOrDir);
        return load(files, parent);
    }

    public static JarClassLoader load(File jarFileOrDir) throws IOException {
        return load(jarFileOrDir, getSystemClassLoader());
    }

    public static JarClassLoader load(List<File> jarFileOrDirs) throws IOException {
        return load(jarFileOrDirs, getSystemClassLoader());
    }

    public static JarClassLoader load(List<File> jarFileOrDirs, ClassLoader parent) throws IOException {
        JarClassLoader loader = new JarClassLoader(parent);
        for (File jarFileOrDir : jarFileOrDirs) {
            loader.addJar(jarFileOrDir);
        }
        return loader;
    }

}
