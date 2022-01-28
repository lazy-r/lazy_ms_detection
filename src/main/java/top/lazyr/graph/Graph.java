package top.lazyr.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.handler.Handler;
import top.lazyr.graph.transformer.Transformer;
import top.lazyr.graph.writer.Writer;
import top.lazyr.model.Edge;
import top.lazyr.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/11/20
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Graph {
    private Transformer transformer;
    private List<Filter> filters;
    private Handler handler;
    private List<Node> nodes;
    private Map<String, Node> nodesMap;
    private List<Writer> writers;
    private ProjectNodeManager nodeManager;



    public Graph createGraph(String projectPath) {
        this.nodes = transformer.transform(projectPath);
        nodeManager = new ProjectNodeManager();
        nodeManager.setNodesMap(nodes);
        for (Filter filter : filters) {
            this.nodes = filter.filter(nodes);
        }
        handler.handle(nodes, nodeManager);
        this.nodesMap = nodes2Map(nodes);
        return this;
    }

    private Map<String, Node> nodes2Map(List<Node> nodes) {
        Map<String, Node> nodesMap = new HashMap<>();
        for (Node node : nodes) {
            nodesMap.put(node.getName(), node);
        }
        return nodesMap;
    }

    public void write() {
        for (Writer writer : writers) {
            writer.write(nodes);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node getNode(String name) {
        return this.nodesMap.get(name);
    }

    public List<Node> getPreNodes(Node srcNode) {
        List<Node> preNodes = new ArrayList<>();
        for (Node node : nodes) {
            List<Edge> edges = node.getEdges();
            if (edges == null) {
                continue;
            }
            for (Edge edge : edges) {
                if (edge.getTargetName().equals(srcNode.getName())) {
                    preNodes.add(this.nodesMap.get(edge.getSrcName()));
                }
            }
        }
        return preNodes;
    }



}
