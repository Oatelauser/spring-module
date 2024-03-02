package com.spring.module.core.context;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-05
 * @since 1.0
 */
public class AnnotationApplicationContextAdaptor extends AnnotationConfigApplicationContext {

    // 虚拟的父应用上下文
    final List<AnnotationConfigApplicationContext> parents = new ArrayList<>();

    public AnnotationApplicationContextAdaptor() {
    }

    public AnnotationApplicationContextAdaptor(DefaultListableBeanFactory beanFactory) {
        super(beanFactory);
    }

    //@Override
    //public void setParent(ApplicationContext parent) {
    //    //if (!CollectionUtils.isEmpty(parentContexts)) {
    //    //    parent = this.adaptApplicationContext(parent);
    //    //}
    //    super.setParent(parent);
    //}

    protected ApplicationContext adaptApplicationContext(ApplicationContext parent) {
        AnnotationApplicationContextAdaptor applicationContextAdaptor = new AnnotationApplicationContextAdaptor();
        applicationContextAdaptor.setParent(parent);
        applicationContextAdaptor.parents.addAll(this.parents);
        return applicationContextAdaptor;
    }

    //@Override
    //protected BeanFactory getInternalParentBeanFactory() {
    //    BeanFactory parentBeanFactory = super.getInternalParentBeanFactory();
    //    if (!CollectionUtils.isEmpty(parents)
    //            && parentBeanFactory instanceof ConfigurableListableBeanFactory parent) {
    //        parentBeanFactory = this.adaptBeanFactory(parent);
    //    }
    //    return parentBeanFactory;
    //}

    //protected BeanFactory adaptBeanFactory(ConfigurableListableBeanFactory parent) {
    //    return new DefaultListableBeanFactoryAdaptor((DefaultListableBeanFactory) parent,
    //            parents.stream().map(GenericApplicationContext::getBeanFactory).toList());
    //}

    public ApplicationContext getRealParent() {
        ApplicationContext parent = super.getParent();
        if (parent instanceof AnnotationApplicationContextAdaptor contextAdaptor) {
            parent = contextAdaptor.getRealParent();
        }
        return parent;
    }

    public void addParent(AnnotationConfigApplicationContext applicationContext) {
        this.parents.add(applicationContext);
    }

    public List<AnnotationConfigApplicationContext> getParents() {
        return parents;
    }

}
