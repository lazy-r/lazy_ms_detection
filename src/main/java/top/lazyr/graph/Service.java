package top.lazyr.graph;

import top.lazyr.model.ApiInfo;
import top.lazyr.model.CallInfo;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.filter.InnerClassFilter;
import top.lazyr.graph.filter.ProjectFilter;
import top.lazyr.graph.handler.FuncHandler;
import top.lazyr.graph.transformer.FuncTransformer;
import top.lazyr.graph.writer.ConsoleWriter;
import top.lazyr.graph.writer.Writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/11/28
 */
public class Service {
    private String svcName;
    private Graph graph;
    private Map<String, List<CallInfo>> callMap;
    private List<ApiInfo> apis;


    public void createSvc(String svcName, String svcPath) {
        this.svcName = svcName;
        List<Filter> filters = new ArrayList<>();
        filters.add(new InnerClassFilter());
        filters.add(new ProjectFilter());
        List<Writer> writers = new ArrayList<>();
        writers.add(new ConsoleWriter());
        this.graph = Graph.builder()
                            .transformer(new FuncTransformer())
                            .filters(filters)
                            .handler(new FuncHandler())
                            .writers(writers)
                            .build();
        this.graph.createGraph(svcPath);
//        this.svcBuilder = new SpringCloudBuilder(svcName);


    }
}
