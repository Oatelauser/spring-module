package com.spring.module.core.parse;

import com.abm.module.api.TriadMetadata;
import com.spring.module.core.module.ModulesRegistrar;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 基于Maven插件的JAR包依赖解析器
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
public class MavenDependencyAnalyzer implements DependencyAnalyzer,
        ApplicationContextAware, SmartInitializingSingleton {

    private static final String EXCLUDE_SCOPE_DEPENDENCY = ":test";

    private ModulesRegistrar modulesRegistrar;
    private ModuleNameResolver moduleNameResolver;
    private ApplicationContext applicationContext;

    @Override
    public ModuleDependency analyzeDependencies(ClassLoader classLoader) throws IOException {
        URL resource = classLoader.getResource(LOCATION_RESOURCE);
        Assert.notNull(resource, "JAR包不存在[" + LOCATION_RESOURCE + "]文件");
        return this.analyzeDependencies(resource);
    }

    @Override
    public ModuleDependency analyzeDependencies(URL resource) throws IOException {
        ModuleDependency moduleDependency = new ModuleDependency();
        Graph<TriadMetadata, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        try (InputStream is = resource.openStream();
             Scanner scanner = new Scanner(new InputStreamReader(is))) {
            while (scanner.hasNextLine()) {
                String[] words = scanner.nextLine().split("\"");
                switch (words.length) {
                    case 1 -> {}
                    case 3 -> this.analyzeModuleInfo(words, moduleDependency);
                    case 5 -> this.analyzeDependency(words, graph);
                    default -> throw new RuntimeException("不符合规范的" + LOCATION_RESOURCE + "文件");
                }
            }
        }

        moduleDependency.setDependencies(graph.vertexSet());
        this.analyzeDependOnModules(graph, moduleDependency);
        return moduleDependency;
    }

    private void analyzeDependency(String[] words, Graph<TriadMetadata, DefaultEdge> graph) {
        String source = words[3];
        if (source.endsWith(EXCLUDE_SCOPE_DEPENDENCY)) {
            return;
        }

        String target = words[1];
        TriadMetadata targetMetadata = TriadMetadata.create(target);
        TriadMetadata sourceMetadata = TriadMetadata.create(source);
        graph.addVertex(targetMetadata);
        graph.addVertex(sourceMetadata);
        graph.addEdge(sourceMetadata, targetMetadata);
    }

    private void analyzeModuleInfo(String[] words, ModuleDependency moduleDependency) {
        String moduleInfo = words[1];
        TriadMetadata metadata = TriadMetadata.create(moduleInfo);
        moduleDependency.setProjectMetadata(metadata);
    }

    private void analyzeDependOnModules(Graph<TriadMetadata, DefaultEdge> graph, ModuleDependency moduleDependency) {
        List<String> dependOnModules = new ArrayList<>();
        for (String moduleName : this.modulesRegistrar.getAllModuleNames()) {
            TriadMetadata vertex = this.moduleNameResolver.resolveModuleName(moduleName);
            if (graph.containsVertex(vertex)) {
                dependOnModules.add(moduleName);
            }
        }

        if (!CollectionUtils.isEmpty(dependOnModules)) {
            moduleDependency.setParentModules(dependOnModules);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.modulesRegistrar = this.applicationContext.getBean(ModulesRegistrar.class);
        this.moduleNameResolver = this.applicationContext.getBean(ModuleNameResolver.class);
    }

    public static void main(String[] args) {
        // 定义顶点
        String a = "springboot";
        String b = "modulea";
        String c = "C";
        String d = "D";
        String e = "E";
        String f = "F";

        // 构建图
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        // 添加顶点
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        //graph.addVertex(d);
        //graph.addVertex(e);
        graph.addVertex(f);

        // 添加边
        // a -> b
        // a -> c -> e -> f
        graph.addEdge(a, b);
        graph.addEdge(b, c);
        //graph.addEdge(a, c);
        //graph.addEdge(c, d);
        //graph.addEdge(d, e);
        //graph.addEdge(b, e);
        //graph.addEdge(e, f);

        //graph.addEdge(b, d);

        //DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        //GraphPath<String, DefaultEdge> graphPath = dijkstraAlg.getPath(c, f);
        //System.out.println(graphPath);
        System.out.println(graph);
        //graph.removeVertex(a);
        System.out.println(graph);

        ConnectivityInspector<String, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        System.out.println(connectivityInspector.pathExists(c, f));
    }

}
