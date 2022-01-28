package top.lazyr.graph.filter;

import top.lazyr.model.Node;

import java.util.List;

public interface Filter {

    /**
     * 过滤无关Node，返回符合条件的Node集合
     * @param nodes
     * @return
     */
    public List<Node> filter(List<Node> nodes);


    /**
     * 若Node符合条件则返回Node，否则返回null
     * @param node
     * @return
     */
    public Node filter(Node node);
}
