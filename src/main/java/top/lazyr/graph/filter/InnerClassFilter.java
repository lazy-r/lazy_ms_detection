package top.lazyr.graph.filter;

import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Level;
import top.lazyr.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class InnerClassFilter implements Filter{
    private static Logger logger = LoggerFactory.getLogger(InnerClassFilter.class);

    @Override
    public List<Node> filter(List<Node> nodes) {
        if (nodes == null || nodes.size() == 0) {
            logger.info("filter nodes that's size is empty.");
            return nodes;
        }

        List<Node> filterNodes = null;
        int level = nodes.get(0).getLevel().intValue();

        switch (level){
            case Level.PACKAGE_LEVEL:
                filterNodes = filterPackageNodes(nodes);
                break;
            case Level.CLASS_LEVEL:
                filterNodes = filterClassNodes(nodes);
                break;
            case Level.FUNC_LEVEL:
                filterNodes = filterFuncNodes(nodes);
                break;
            default:
                filterNodes = nodes;
                logger.info("no filter, because node's level(" + level +") is not exist.");
        }

        return filterNodes;
    }

    @Override
    public Node filter(Node node) {
        int level = node.getLevel().intValue();
        Node filterNode = null;
        switch (level){
            case Level.PACKAGE_LEVEL:
                filterNode = filterPackageNode(node);
                break;
            case Level.CLASS_LEVEL:
                filterNode = filterClassNode(node) ? null : node;
                break;
            case Level.FUNC_LEVEL:
                filterNode = filterFuncNode(node) ? null : node;
                break;
            default:
                filterNode = node;
                logger.info("no filter, because node's level(" + level +") is not exist.");
        }
        return filterNode;
    }

    private List<Node> filterPackageNodes(List<Node> nodes) {
        List<Node> filterNodes = new ArrayList<>();
        for (Node<List<Node>> node : nodes) {
            Node<List<Node>> filterNode = filterPackageNode(node);
            if (filterNode.getNodeInfo().size() != 0) {
                filterNodes.add(filterNode);
            }
        }
        return filterNodes;
    }

    private List<Node> filterClassNodes(List<Node> classNodes) {
        List<Node> filterNodes = new ArrayList<>();

        for (Node<CtClass> classNode : classNodes) {
            if (!filterClassNode(classNode)) {
                filterNodes.add(classNode);
            }
        }

        return filterNodes;
    }

    private List<Node> filterFuncNodes(List<Node> funcNodes) {
        List<Node> filterNodes = new ArrayList<>();

        for (Node<CtClass> funcNode : funcNodes) {
            if (!filterFuncNode(funcNode)) {
                filterNodes.add(funcNode);
            }
        }

        return filterNodes;
    }

    private Node  filterPackageNode(Node packageNode) {
        List<Node> classNodes = (List<Node>) packageNode.getNodeInfo();
        List<Node> filterClassNodes = new ArrayList<>();
        for (Node classNode : classNodes) {
            if (!filterClassNode(classNode)) {
                filterClassNodes.add(classNode);
            }
        }
        packageNode.setNodeInfo(filterClassNodes);
        return packageNode;
    }



    private boolean filterClassNode(Node classNode) {
        return classNode.getName().contains("$");
    }

    public boolean filterFuncNode(Node funcNode) {
        return funcNode.getName().contains("$");
    }


}
