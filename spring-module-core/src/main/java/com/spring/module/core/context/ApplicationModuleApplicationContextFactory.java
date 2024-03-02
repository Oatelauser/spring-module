package com.spring.module.core.context;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import static org.springframework.boot.WebApplicationType.NONE;

/**
 * 构建模块应用的上下文
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-07
 * @since 1.0
 */
public class ApplicationModuleApplicationContextFactory implements ApplicationContextFactory {

    @Override
    public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
        return NONE.equals(webApplicationType) ? new AnnotationApplicationModuleContext() : null;
    }

}
