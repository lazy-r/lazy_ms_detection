package top.lazyr.graph.transformer;

import top.lazyr.graph.CtClassManager;
import top.lazyr.graph.ProjectNodeManager;
import top.lazyr.model.Node;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/20
 */
public abstract class Transformer {
    public abstract List<Node> transform(String projectPath);
    protected CtClassManager manager;

    public Transformer() {
        this.manager = CtClassManager.getCtClassManager();
    }


}
