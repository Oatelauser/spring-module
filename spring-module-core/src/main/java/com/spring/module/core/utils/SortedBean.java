package com.spring.module.core.utils;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.lang.NonNull;

/**
 * 对Bean对象进行排序的代理类
 * <p>
 * 有些bean无法使用Spring Ordered接口或者@Order注解
 *
 * @author DearYang
 * @date 2022-08-17
 * @since 1.0
 */
public class SortedBean<T> extends AnnotationAwareOrderComparator implements Comparable<SortedBean<T>>, Ordered {

	private final T bean;

	public SortedBean(T bean) {
		this.bean = bean;
	}

	@Override
	public int compareTo(@NonNull SortedBean other) {
		return compare(this, other);
	}

	@Override
	public int getOrder() {
		int classOrder = getOrder(bean);
		int annotationOrder = OrderUtils.getOrder(bean.getClass(), Ordered.LOWEST_PRECEDENCE);
		return Math.min(classOrder, annotationOrder);
	}

	public T getBean() {
		return bean;
	}

}
