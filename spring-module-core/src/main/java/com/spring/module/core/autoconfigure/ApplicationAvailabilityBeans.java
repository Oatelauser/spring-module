package com.spring.module.core.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * 解决子应用上下文注册可用性问题，同时解决子上下文内存泄漏
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-12
 * @since 1.0
 */
public class ApplicationAvailabilityBeans extends ApplicationAvailabilityBean implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(AvailabilityChangeEvent<?> event) {
        // 如果不是父上下文则不注册，否则会产生子上下文的内存泄漏
        if (event.getSource() instanceof ApplicationContext context
                && context == this.applicationContext) {
            super.onApplicationEvent(event);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
