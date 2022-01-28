package top.lazyr.graph;

import top.lazyr.graph.filter.Filter;
import top.lazyr.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/12/1
 */
public class ProjectNodeManager {
    private Map<String, Node> nodesMap;
    private List<Node> nodes;

    public ProjectNodeManager() {
        this.nodesMap = new HashMap<>();
        this.nodes = new ArrayList<>();
    }

    public void setNodesMap(List<Node> nodes) {
        this.nodes = nodes;
        for (Node node : nodes) {
            nodesMap.put(node.getName(), node);
        }
    }



    public boolean isProjectNode(String nodeName) {
        return nodesMap.containsKey(nodeName);
    }

    public Node findNode(String nodeName) {
        return nodesMap.get(nodeName);
    }



}
