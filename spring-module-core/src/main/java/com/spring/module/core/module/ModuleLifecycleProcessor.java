package com.spring.module.core.module;

import com.abm.module.api.SmartModuleLifecycle;
import com.spring.module.core.utils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 模块生命周期处理器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
@Order
public class ModuleLifecycleProcessor implements ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(ModuleLifecycleProcessor.class);

    private List<SmartModuleLifecycle> lifecycles;

    public void startModule() {
        if (CollectionUtils.isEmpty(lifecycles)) {
            return;
        }
        for (SmartModuleLifecycle lifecycle : lifecycles) {
            try {
                lifecycle.startModule();
            } catch (Exception e) {
                LOG.error("执行startModule方法异常", e);
            }
        }
    }

    public void stopModule() {
        if (CollectionUtils.isEmpty(lifecycles)) {
            return;
        }
        for (SmartModuleLifecycle lifecycle : lifecycles) {
            try {
                lifecycle.stopModule();
            } catch (Exception e) {
                LOG.error("执行stopModule方法异常", e);
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.lifecycles = BeanUtils.sort(applicationContext, SmartModuleLifecycle.class);
    }

}
