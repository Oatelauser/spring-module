package com.spring.module.core.loading;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 支持多个父类类加器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public class ZipClassLoader extends JarClassLoader {

    private final List<JarClassLoader> parent = new ArrayList<>();

    public ZipClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            if (CollectionUtils.isEmpty(parent)) {
                throw e;
            }
        }

        for (JarClassLoader classLoader : parent) {
            try {
                return classLoader.loadClass(name);
            } catch (Throwable ex) {  // 说明已经被父类加载器加载过
                return classLoader.findClass(name);
            }
        }
        throw new ClassNotFoundException(name);
    }

    public void addParent(JarClassLoader parent) {
        this.parent.add(parent);
    }

    public List<JarClassLoader> getParents() {
        return parent;
    }

}
