package com.spring.module.core.module;

import com.abm.module.api.SpringModule;
import org.springframework.context.ApplicationContext;

/**
 * 模块监听后置处理器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
public interface ModulePostProcessor {

    /**
     * 加载模块前处理
     *
     * @param springModule       模块的信息
     * @param applicationContext 未初始化的模块的应用上下文
     */
    void postProcessBeforeModule(SpringModule springModule, ApplicationContext applicationContext);

    /**
     * 加载模块后处理
     *
     * @param applicationContext 模块的应用上下文
     */
    void postProcessAfterModule(ApplicationContext applicationContext);

}
