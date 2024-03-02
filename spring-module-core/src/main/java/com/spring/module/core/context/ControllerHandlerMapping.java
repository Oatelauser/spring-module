package com.spring.module.core.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 自定义手动注册controller路由
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @see RequestMappingHandlerMapping
 * @since 1.0
 */
public class ControllerHandlerMapping extends RequestMappingHandlerMapping {

    private static final Log LOG = LogFactory.getLog(ControllerHandlerMapping.class);
    private final Map<Class<?>, List<RequestMappingInfo>> requestMappingInfos = new ConcurrentHashMap<>();

    /**
     * 注册controller路由
     *
     * @param controller 请求控制器
     */
    public void registerController(@NonNull Object controller, Predicate<Object> filter) {
        if (filter.test(controller)) {
            this.detectHandlerMethods(controller);
            LOG.info("控制器注册成功：" + controller);
        }
    }

    /**
     * 注销controller路由
     *
     * @param controllerClass 请求控制器类型
     */
    public void unregisterController(Class<?> controllerClass) {
        List<RequestMappingInfo> requestMappingInfos = this.requestMappingInfos.remove(controllerClass);
        if (!CollectionUtils.isEmpty(requestMappingInfos)) {
            requestMappingInfos.forEach(this::unregisterMapping);
            LOG.info("注销控制器成功：" + controllerClass);
        }
    }

    public void unregisterController(Class<?> controllerClass, Predicate<Class<?>> filter) {
        if (filter.test(controllerClass)) {
            this.unregisterController(controllerClass);
        }
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
        RequestMappingInfo requestMappingInfo = super.getMappingForMethod(method, handlerType);
        if (requestMappingInfo != null) {
            requestMappingInfos.computeIfAbsent(handlerType, key ->
                    new ArrayList<>()).add(requestMappingInfo);
        }
        return requestMappingInfo;
    }

}
