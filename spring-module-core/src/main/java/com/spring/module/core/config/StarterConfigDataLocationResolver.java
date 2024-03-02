package com.spring.module.core.config;

import com.abm.module.api.SpringModule;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static org.springframework.boot.context.config.ConfigDataLocation.OPTIONAL_PREFIX;

/**
 * 拓展模块上下文也支持SpringBoot配置的加载机制
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-15
 * @since 1.0
 */
public class StarterConfigDataLocationResolver extends StandardConfigDataLocationResolver {

    public StarterConfigDataLocationResolver(DeferredLogFactory logFactory,
            Binder binder, ResourceLoader resourceLoader) {
        super(logFactory, binder, resourceLoader);
    }

    @Override
    public List<StandardConfigDataResource> resolve(ConfigDataLocationResolverContext context,
            ConfigDataLocation location) throws ConfigDataNotFoundException {
        location = this.adaptConfigDataLocation(location, context);
        return super.resolve(context, location);
    }

    @Override
    public List<StandardConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext context,
            ConfigDataLocation location, Profiles profiles) {
        location = this.adaptConfigDataLocation(location, context);
        return super.resolveProfileSpecific(context, location, profiles);
    }

    protected ConfigDataLocation adaptConfigDataLocation(ConfigDataLocation location,
            ConfigDataLocationResolverContext context) {
        String locationValue = location.getValue();
        if (locationValue.startsWith("classpath")) {
            return location;
        }

        ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
        SpringModule springModule = bootstrapContext.get(SpringModule.class);
        String parent = springModule.getJarFile().getParent();
        locationValue = locationValue.replace("./", parent);
        location = ConfigDataLocation.of(OPTIONAL_PREFIX + locationValue);
        return location;
    }

}
