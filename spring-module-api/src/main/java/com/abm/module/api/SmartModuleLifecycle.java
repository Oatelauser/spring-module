package com.abm.module.api;

/**
 * 模块的生命周期监听器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
public interface SmartModuleLifecycle {

    /**
     * 是否在运行中
     *
     * @return true-运行中，false-停止
     */
    boolean isRunning();

    /**
     * 启动模块
     */
    void startModule();

    /**
     * 停止模块
     */
    void stopModule();

}
