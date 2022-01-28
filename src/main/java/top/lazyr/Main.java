package top.lazyr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.graph.Graph;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.filter.InnerClassFilter;
import top.lazyr.graph.filter.ProjectFilter;
import top.lazyr.graph.handler.*;
import top.lazyr.graph.transformer.*;
import top.lazyr.graph.writer.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/21
 */
public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ClassNotFoundException {
        // 这里填写编译后项目的绝对路径
        buildFZJ("/Users/lazyr/Work/projects/devops/test/data/dop");
    }

    public static void buildFZJ(String projectPath) {
        List<Filter> filters = new ArrayList<>();
//        filters.add(new InnerClassFilter());
//        filters.add(new ProjectFilter());
        List<Writer> writers = new ArrayList<>();
        writers.add(new FZJSvcInfoWriter("fzj_1.xlsx"));
        writers.add(new FZJDependencyWriter("fzj_2.xlsx"));
        Handler handler = new FZJSvcHandler();
        List<Filter> handlerFilters = new ArrayList<>();
//        handlerFilters.add(new InnerClassFilter());
//        handlerFilters.add(new ProjectFilter());
        handler.setFilters(handlerFilters);
        Graph graph = Graph.builder()
                .transformer(new FZJSvcTransformer())
                .filters(filters)
                .handler(handler)
                .writers(writers)
                .build();
        graph.createGraph(projectPath);
        graph.write();
    }

    public static void buildZX(String projectPath) {
        List<Filter> filters = new ArrayList<>();
//        filters.add(new InnerClassFilter());
//        filters.add(new ProjectFilter());
        List<Writer> writers = new ArrayList<>();
        writers.add(new ZXMSWriter());
        Handler handler = new SvcHandler();
        List<Filter> handlerFilters = new ArrayList<>();
//        handlerFilters.add(new InnerClassFilter());
//        handlerFilters.add(new ProjectFilter());
        handler.setFilters(handlerFilters);
        Graph graph = Graph.builder()
                .transformer(new SvcTransformer())
                .filters(filters)
                .handler(handler)
                .writers(writers)
                .build();
        graph.createGraph(projectPath);
        graph.write();
    }


}
