package com.spring.module.core.loading;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * 缓存{@link Enumeration}的代理类
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-10
 * @since 1.0
 */
public class ArrayEnumeration<E> implements Enumeration<E> {

    private Iterator<E> enumeration;
    private final List<E> enumerations;

    public ArrayEnumeration(Enumeration<E> enumeration) {
        List<E> enumerations = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            E element = enumeration.nextElement();
            enumerations.add(element);
        }
        this.enumerations = enumerations;
    }

    public ArrayEnumeration(List<E> enumerations) {
        this.enumerations = enumerations;
    }

    public ArrayEnumeration<E> add(List<E> autoConfigurationResources) {
        ArrayList<E> enumerations = new ArrayList<>(this.enumerations);
        enumerations.addAll(autoConfigurationResources);
        return new ArrayEnumeration<>(enumerations);
    }

    public List<E> getEnumerations() {
        return enumerations;
    }

    public Enumeration<E> asEnumeration() {
        enumeration = List.copyOf(enumerations).iterator();
        return this;
    }

    @Override
    public boolean hasMoreElements() {
        return enumeration.hasNext();
    }

    @Override
    public E nextElement() {
        return enumeration.next();
    }

}
