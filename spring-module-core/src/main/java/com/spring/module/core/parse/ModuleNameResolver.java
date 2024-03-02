package com.spring.module.core.parse;


import com.abm.module.api.TriadMetadata;

/**
 * 模块名解析器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-03
 * @since 1.0
 */
public interface ModuleNameResolver {

    /**
     * 通过JAR包文件获取模块名
     *
     * @param metadata {@link TriadMetadata}
     * @return 模块名
     */
    String obtainModuleName(TriadMetadata metadata);

    /**
     * 解析模块名的元数据三元组信息
     *
     * @param moduleName 模块名
     * @return {@link TriadMetadata}
     */
    TriadMetadata resolveModuleName(String moduleName);

}
