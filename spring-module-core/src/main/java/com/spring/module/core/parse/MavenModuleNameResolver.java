package com.spring.module.core.parse;

import com.abm.module.api.TriadMetadata;
import org.springframework.util.Assert;


/**
 * 基于maven pom文件的三元组模块名解析器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public class MavenModuleNameResolver implements ModuleNameResolver {

    @Override
    public String obtainModuleName(TriadMetadata metadata) {
        return metadata.groupId() + ":" + metadata.artifactId() + ":" + metadata.version();
    }

    @Override
    public TriadMetadata resolveModuleName(String moduleName) {
        Assert.hasText(moduleName, "模块名不能为空字符串");
        String[] split = moduleName.split(":");
        return new TriadMetadata(split[0], split[1], split[2]);
    }

}
