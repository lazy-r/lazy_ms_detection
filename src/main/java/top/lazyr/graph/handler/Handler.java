package top.lazyr.graph.handler;


import top.lazyr.graph.ProjectNodeManager;
import top.lazyr.graph.filter.Filter;
import top.lazyr.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class Handler {
    protected Map<String, Node> nodesMap;
    protected List<Filter> filters;

    public abstract void handle(List<Node> nodes, ProjectNodeManager nodeManager);

    protected Map<String, Node> nodes2Map(List<Node> classNodes) {
        Map<String, Node> nodesMap = new HashMap<>();
        for (Node classNode : classNodes) {
            nodesMap.put(classNode.getName(), classNode);
        }
        return nodesMap;
    }

    protected Node findNode(String className) {
        return nodesMap.getOrDefault(className, null);
    }


    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
}
