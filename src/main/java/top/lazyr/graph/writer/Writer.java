package top.lazyr.graph.writer;

import top.lazyr.model.Node;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/20
 */
public abstract class Writer {
    public abstract void write(List<Node> nodes);
}
