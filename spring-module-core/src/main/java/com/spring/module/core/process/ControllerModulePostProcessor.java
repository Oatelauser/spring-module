package com.spring.module.core.process;

import com.abm.module.api.SpringModule;
import com.spring.module.core.context.AnnotationApplicationModuleContext;
import com.spring.module.core.context.ControllerHandlerMapping;
import com.spring.module.core.module.DisposableModule;
import com.spring.module.core.module.ModulePostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 模块接口的注册处理器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-08
 * @since 1.0
 */
public class ControllerModulePostProcessor implements DisposableModule, ModulePostProcessor {

    private final ControllerHandlerMapping controllerHandlerMapping;

    public ControllerModulePostProcessor(ControllerHandlerMapping controllerHandlerMapping) {
        this.controllerHandlerMapping = controllerHandlerMapping;
    }

    @Override
    public void postProcessBeforeModule(SpringModule springModule, ApplicationContext rootContext) {
    }

    @Override
    public void postProcessAfterModule(ApplicationContext applicationContext) {
        if (applicationContext instanceof AnnotationApplicationModuleContext) {
            Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(Controller.class);
            if (!CollectionUtils.isEmpty(controllerBeans)) {
                controllerBeans.values().forEach(controllerBean ->
                        controllerHandlerMapping.registerController(controllerBean, controller ->
                                Objects.equals(applicationContext.getClassLoader(), controller.getClass().getClassLoader())));
            }
        }
    }

    @Override
    public void destroy(ApplicationContext applicationContext) {
        if (applicationContext instanceof AnnotationApplicationModuleContext) {
            Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(Controller.class);
            if (!CollectionUtils.isEmpty(controllerBeans)) {
                controllerBeans.values().forEach(controllerBean -> {
                    Class<?> controllerClass = controllerBean.getClass();
                    controllerHandlerMapping.unregisterController(controllerClass, controller ->
                            Objects.equals(applicationContext.getClassLoader(), controller.getClassLoader()));
                });
            }
        }
    }

}
