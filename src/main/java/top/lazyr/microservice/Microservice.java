package top.lazyr.microservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.graph.Graph;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.filter.InnerClassFilter;
import top.lazyr.graph.filter.ProjectFilter;
import top.lazyr.graph.handler.FuncHandler;
import top.lazyr.graph.transformer.FuncTransformer;
import top.lazyr.graph.writer.ConsoleWriter;
import top.lazyr.graph.writer.Writer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class Microservice {
    private static Logger logger = LoggerFactory.getLogger(Microservice.class);
    public String projectPath;
    private List<Graph> services;

    public Microservice(String projectPath) {
        this.projectPath = projectPath;
    }


    public static void build(String projectPath) {
        File catalog = new File(projectPath);
        if (!catalog.exists()) {
            logger.info(projectPath + " not exist");
            return;
        }
        if (!catalog.isDirectory()) {
            logger.info(projectPath + " is not catalog");
            return;
        }

        File[] svcCatalogs = catalog.listFiles();
        for (File svcCatalog : svcCatalogs) {
            if (!svcCatalog.isDirectory()) {
                continue;
            }
            buildDependence(catalog.getName(), svcCatalog.getName(), svcCatalog.getAbsolutePath());
        }
    }

    public static void buildDependence(String projectName, String svcName,String svcPath) {
        List<Filter> filters = new ArrayList<>();
        filters.add(new InnerClassFilter());
        filters.add(new ProjectFilter());
        List<Writer> writers = new ArrayList<>();
        writers.add(new ConsoleWriter());

        Graph graph = Graph.builder()
                .transformer(new FuncTransformer())
                .filters(filters)
                .handler(new FuncHandler())
                .writers(writers)
                .build();
        graph.createGraph(svcPath);

    }
}

