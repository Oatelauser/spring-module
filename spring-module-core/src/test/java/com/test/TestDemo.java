package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;

import static org.springframework.core.io.support.SpringFactoriesLoader.forResourceLocation;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public class TestDemo {

    @Test
    public void t() throws Exception {
        URL url = Paths.get("E:\\spring-module-core-0.0.1-SNAPSHOT.jar").toUri().toURL();
        JarClassLoader classLoader = new JarClassLoader(null);
        classLoader.addURL(url);
        Enumeration<URL> resources = classLoader.getResources("META-INF/spring.factories");
        SpringFactoriesLoader springFactoriesLoader = forResourceLocation("META-INF/spring.factories", classLoader);


        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources1 = resolver.getResources("META-INF/maven/**/pom.xml");
        int a = 1;
    }

}
