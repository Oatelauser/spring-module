package com.abm.module.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块之间的依赖树
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public class ModuleTree {

    private final TriadMetadata metadata;
    private final List<ModuleTree> parent = new ArrayList<>();

    public ModuleTree(TriadMetadata metadata) {
        this.metadata = metadata;
    }

    public void addParent(TriadMetadata metadata) {
        this.parent.add(new ModuleTree(metadata));
    }

    public void addParent(ModuleTree parent) {
        this.parent.add(parent);
    }

    public boolean isLeave() {
        return parent.isEmpty();
    }

    public TriadMetadata getMetadata() {
        return metadata;
    }

    public List<ModuleTree> getParent() {
        return parent;
    }

}
