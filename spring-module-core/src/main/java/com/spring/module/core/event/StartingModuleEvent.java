package com.spring.module.core.event;

import com.spring.module.core.context.AnnotationApplicationModuleContext;
import org.springframework.context.ApplicationEvent;

/**
 * 模块正在启动的事件通知
 * <p>
 * 模块上下文创建完成，但未初始化{@link AnnotationApplicationModuleContext#refresh()}
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-05
 * @since 1.0
 */
public class StartingModuleEvent extends ApplicationEvent {

    private final AnnotationApplicationModuleContext applicationContext;

    public StartingModuleEvent(AnnotationApplicationModuleContext applicationContext) {
        super(applicationContext.getSpringModule());
        this.applicationContext = applicationContext;
    }

    public AnnotationApplicationModuleContext getApplicationContext() {
        return applicationContext;
    }

}
