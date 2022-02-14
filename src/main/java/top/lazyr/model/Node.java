package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/20
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Node<T> {
    private T nodeInfo;
    private String name;
    /** SERVICE/PACKAGE/CLASS/FUNC */
    private Level level;
    // TODO: 代优化为枚举
    // PROJECT
    private String from;
    // 传入依赖数
    private int afferentNum;
    // 传出依赖数
    private int efferentNum;
    private List<Edge> edges;



    public void addEdge(String srcName, Node<T> targetNode, String type, int weight) {
        if (this.edges == null) {
            this.edges = new ArrayList<>();
        }

        for (Edge edge : edges) {
            if (edge.getTargetName().equals(targetNode.getName()) && edge.getType().equals(type)) {
                buildEdge(targetNode, edge, weight);
                return;
            }
        }

        buildEdge(srcName, targetNode, type, weight);
    }



    public void addAfferentNum(int afferentNum) {
        this.afferentNum += afferentNum;
    }

    public void addEfferentNum(int efferentNum) {
        this.efferentNum += efferentNum;
    }

    private void buildEdge(Node<T> targetNode, Edge edge, int weight) {
        // 目标节点传入依赖数加weight
        targetNode.addAfferentNum(weight);
        // 边的权重加weight
        edge.addWeight(weight);
        // 本节点的传出依赖数加weight
        this.addEfferentNum(weight);
    }

    private void buildEdge(String srcName, Node<T> targetNode, String type, int weight) {
        // 目标节点传入依赖数加weight
        targetNode.addAfferentNum(weight);
        // 创建边
        Edge edge = new Edge(srcName, targetNode.getName(), type, weight);
        this.edges.add(edge);
        // 本节点的传出依赖数加weight
        this.addEfferentNum(weight);
    }

    @Override
    public String toString() {
        return "Node{" + "\n" +
                "\tname='" + name + '\'' + "\n" +
                "\tlevel=" + level + "\n" +
                "\tfrom='" + from + '\'' + "\n" +
                "\tnodeInfo=" + nodeInfo + "\n" +
                "\tafferentNum=" + afferentNum + "\n" +
                "\tefferentNum=" + efferentNum + "\n" +
                "\tedges=" + edges + "\n" +
                '}';
    }
}
