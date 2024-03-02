package com.spring.module.core.loading;

import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 基于Spring的要求做定制化的类加载器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-07
 * @since 1.0
 */
//TODO:加载完整的模块包，包含依赖
public class SpringClassLoader extends ZipClassLoader {

    public static final ArrayEnumeration<URL> AUTO_CONFIGURATION_RESOURCES;
    public static final String AUTO_CONFIGURATION_FILE = "META-INF/spring.factories";
    public static final Set<String> SPRING_SPI_RESOURCES = Set.of(
            AUTO_CONFIGURATION_FILE,
            "META-INF/spring.components",
            "META-INF/spring-autoconfigure-metadata.properties",
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
    );

    static {
        try {
            Enumeration<URL> resources = SpringClassLoader.class.getClassLoader().getResources(AUTO_CONFIGURATION_FILE);
            AUTO_CONFIGURATION_RESOURCES = new ArrayEnumeration<>(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, ArrayEnumeration<URL>> locationResources;

    public SpringClassLoader(ClassLoader parent) {
        super(parent);
    }

    public SpringClassLoader(File jarFile, ClassLoader parent) throws IOException {
        this(parent);
        this.addJar(jarFile);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (!SPRING_SPI_RESOURCES.contains(name)) {
            return super.getResources(name);
        }
        ArrayEnumeration<URL> localEnumeration = this.getLocationEnumerations(name);
        if (localEnumeration == null) {
            return super.getResources(name);
        }

        if (AUTO_CONFIGURATION_FILE.equals(name)) {
            localEnumeration = localEnumeration.add(AUTO_CONFIGURATION_RESOURCES.getEnumerations());
        }
        return localEnumeration.asEnumeration();
    }

    private ArrayEnumeration<URL> getLocationEnumerations(String name) {
        ArrayEnumeration<URL> locationEnumeration = null;
        if (!CollectionUtils.isEmpty(locationResources)) {
            locationEnumeration = locationResources.get(name);
        }
        return locationEnumeration;
    }

    public Map<String, ArrayEnumeration<URL>> getLocationResources() {
        return locationResources;
    }

    public void setLocationResources(Map<String, ArrayEnumeration<URL>> locationResources) {
        this.locationResources = locationResources;
    }

}
