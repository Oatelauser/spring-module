package com.spring.module.core.module;

import com.spring.module.core.context.AnnotationApplicationModuleContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.spring.module.core.exception.ModuleRegistryException.*;
import static com.spring.module.core.module.ModulesRegistrar.DependencyStrategy.DESC;

/**
 * 图结构的模块注册器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-09
 * @since 1.0
 */
public abstract class AbstractGraphModulesRegistrar extends ModulesRegistrarSupport
        implements DisposableBean, ModulesRegistrar {

    private static final Log LOG = LogFactory.getLog(AbstractGraphModulesRegistrar.class);

    /**
     * 不涉及Root应用上下文
     */
    protected volatile Graph<String, DefaultEdge> graph;
    private final Map<String, Set<String>> parentModules = new ConcurrentHashMap<>();
    private final Map<String/*模块名*/, AnnotationApplicationModuleContext> applicationModuleContexts = new ConcurrentHashMap<>();

    public AbstractGraphModulesRegistrar() {
        this.graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
    }

    @Override
    public Set<String> getAllModuleNames() {
        return Set.copyOf(applicationModuleContexts.keySet());
    }

    @Override
    public AnnotationApplicationModuleContext applicationContext(String moduleName) {
        return applicationModuleContexts.get(moduleName);
    }

    @Override
    public AnnotationApplicationModuleContext installModuleApplication(AnnotationApplicationModuleContext applicationContext) {
        String moduleName = applicationContext.getId();
        if (applicationModuleContexts.containsKey(moduleName)) {
            alreadyExistModule("模块重复注册: " + moduleName);
        }

        this.graph = this.mergeModuleGraph(applicationContext);
        this.applicationModuleContexts.put(moduleName, applicationContext);
        return applicationContext;
    }

    @Override
    public void uninstallModule(String moduleName) {
        if (!applicationModuleContexts.containsKey(moduleName)) {
            notExistModule("模块[" + moduleName + "]未注册不能卸载");
        }
        if (graph.outDegreeOf(moduleName) > 0) {
            existParentsModule("模块[" + moduleName + "]被其他模块引用无法卸载");
        }

        AnnotationApplicationModuleContext applicationContext = applicationModuleContexts.remove(moduleName);
        if (applicationContext != null) {
            this.uninstallModule(moduleName, applicationContext);
        }
    }

    protected void uninstallModule(String moduleName, AnnotationApplicationModuleContext applicationContext) {
        this.parentModules.remove(moduleName);
        graph.removeVertex(moduleName);
        applicationContext.close();

        if (applicationContext.getClassLoader() instanceof URLClassLoader classLoader) {
            try {
                classLoader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        LOG.info("卸载模块成功：" + applicationContext.getId());
    }

    @Override
    public Graph<String, DefaultEdge> obtainModuleDependencies(DependencyStrategy strategy) {
        if (DESC.equals(strategy)) {
            Graph<String, DefaultEdge> reversedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
            Graphs.addGraphReversed(reversedGraph, this.graph);
            return reversedGraph;
        }
        return this.graph;
    }

    private Graph<String, DefaultEdge> mergeModuleGraph(AnnotationApplicationModuleContext applicationContext) {
        Graph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        Graphs.addGraph(graph, this.graph);
        String targetVertex = applicationContext.getId();
        graph.addVertex(targetVertex);
        Set<String> currentModuleDependencies = this.parentModules.computeIfAbsent(targetVertex, key -> new HashSet<>());

        for (AnnotationConfigApplicationContext parent : applicationContext.getParents()) {
            if (parent instanceof AnnotationApplicationModuleContext applicationModuleContext) {
                String sourceVertex = applicationModuleContext.getId();
                graph.addEdge(sourceVertex, targetVertex);
                currentModuleDependencies.add(sourceVertex);
                currentModuleDependencies.addAll(this.parentModules.get(sourceVertex));
            }
        }
        return graph;
    }

    @Override
    protected List<String> mergeModules(List<String> modules) {
        if (CollectionUtils.isEmpty(modules)) {
            return List.of();
        }
        Set<String> deleteModules = new HashSet<>();
        for (String module : modules) {
            Set<String> dependencies = this.parentModules.get(module);
            modules.stream().filter(dependencies::contains).forEach(deleteModules::add);
        }
        modules.removeAll(deleteModules);
        return modules;
    }

    @Override
    public void destroy() {
        Graph<String, DefaultEdge> graph = this.obtainModuleDependencies(DESC);
        GraphIterator<String, DefaultEdge> graphIterator = new TopologicalOrderIterator<>(graph);
        graphIterator.forEachRemaining(id -> applicationModuleContexts.remove(id).close());
    }

    protected Map<String, AnnotationApplicationModuleContext> getApplicationModuleContexts() {
        return applicationModuleContexts;
    }

}
