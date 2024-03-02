package com.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
public class JarClassLoader extends URLClassLoader {

    // 开启并行加载
    static {
        registerAsParallelCapable();
    }

    public JarClassLoader(ClassLoader parent) {
        // 传入空的URL，具体资源通过addURL传入
        super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public void addURL(File file) {
        addURL(file.toURI());
    }

    public void addURL(URI uri) {
        try {
            addURL(uri.toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加jar包、指定目录及其子目录下的所有jar包
     * 深度优先策略遍历目录查找jar包
     *
     * @param jarFileOrDir
     * @throws IOException
     */
    public void addJar(File jarFileOrDir) throws IOException {
        if (isJarFile(jarFileOrDir)) {
            addURL(jarFileOrDir);
            return;
        }

        Files.walk(jarFileOrDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                .map(p -> p.toUri())
                .filter(JarClassLoader::isJarFile)
                .forEach(this::addURL);
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
        return path.toLowerCase().endsWith(".jar");
    }

    // 加载资源
    public static JarClassLoader load(File jarFileOrDir, ClassLoader parent) {
        List<File> files = Collections.singletonList(jarFileOrDir);
        return load(files, parent);
    }

    public static JarClassLoader load(File jarFileOrDir) {
        return load(jarFileOrDir, getSystemClassLoader());
    }

    public static JarClassLoader load(List<File> jarFileOrDirs) {
        return load(jarFileOrDirs, getSystemClassLoader());
    }

    public static JarClassLoader load(List<File> jarFileOrDirs, ClassLoader parent) {
        JarClassLoader loader = new JarClassLoader(getSystemClassLoader());
        for (File jarFileOrDir : jarFileOrDirs) {
            try {
                loader.addJar(jarFileOrDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return loader;
    }

}
