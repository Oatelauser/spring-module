package com.spring.module.core.autoconfigure;

import com.spring.module.core.context.ControllerHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 修改Spring MVC的配置
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-12
 * @since 1.0
 */
public class WebMvcConfigurerRegistrations implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ControllerHandlerMapping();
    }

}
