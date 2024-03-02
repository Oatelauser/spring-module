package com.spring.module.core.module;

import org.springframework.context.ApplicationContext;

/**
 * 模块销毁后的后置处理
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
public interface DisposableModule {

    /**
     * 销毁模块的后置处理
     *
     * @param applicationContext 模块应用上下文
     * @throws Exception 销毁过程的异常
     */
    void destroy(ApplicationContext applicationContext) throws Exception;

}
