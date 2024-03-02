package com.spring.module.core.parse;

import com.abm.module.api.TriadMetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 模块依赖信息描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-16
 * @since 1.0
 */
public class ModuleDependency {

    private TriadMetadata projectMetadata;

    /**
     * 依赖是包含模块自身
     */
    private Set<TriadMetadata> dependencies;

    private List<String> parentModules;

    public TriadMetadata getProjectMetadata() {
        return projectMetadata;
    }

    public void setProjectMetadata(TriadMetadata projectMetadata) {
        this.projectMetadata = projectMetadata;
    }

    public Set<TriadMetadata> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<TriadMetadata> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getParentModules() {
        return parentModules;
    }

    public void setParentModules(List<String> parentModules) {
        this.parentModules = parentModules;
    }

    public Set<TriadMetadata> copyExcludeSelfDependencies() {
        Set<TriadMetadata> dependencies = new HashSet<>(this.dependencies);
        dependencies.remove(projectMetadata);
        return dependencies;
    }

}