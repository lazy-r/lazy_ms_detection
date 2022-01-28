package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/11/20
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class Edge {
    private String srcName;
    private String targetName;
    private Node node;
    private String type;
    private int weight;

    public Edge(String srcName, String targetName, String type, int weight) {
        this.targetName = targetName;
        this.srcName = srcName;
        this.type = type;
        this.weight = weight;
    }

//    public Edge(Node node, String type, int weight) {
//        this.node = node;
//        this.name = node.getName();
//        this.type = type;
//        this.weight = weight;
//    }

    public void addWeight(int weight) {
        this.weight += weight;
    }
}
