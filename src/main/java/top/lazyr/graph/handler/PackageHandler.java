package top.lazyr.graph.handler;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.graph.CtClassManager;
import top.lazyr.graph.ProjectNodeManager;
import top.lazyr.graph.filter.Filter;
import top.lazyr.model.Level;
import top.lazyr.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/11/21
 */
public class PackageHandler extends Handler {
    private static Logger logger = LoggerFactory.getLogger(PackageHandler.class);
    private Map<String, Node> jdkNodesMap = new HashMap<>();
    private Map<String, Node> outerNodesMap = new HashMap<>();
    private CtClassManager manager = CtClassManager.getCtClassManager();
    private ProjectNodeManager nodeManager;



    @Override
    public void handle(List<Node> packageNodes, ProjectNodeManager nodeManager) {
        this.nodesMap = nodes2Map(packageNodes);
        this.nodeManager = nodeManager;


        for (Node packageNode: packageNodes) {
            buildNode(packageNode);
        }


        List<Node> jdkNodes = new ArrayList<>();
        List<Node> outerNodes = new ArrayList<>();

        for (String nodeName : jdkNodesMap.keySet()) {
            jdkNodes.add(jdkNodesMap.get(nodeName));
        }
        for (String nodeName : outerNodesMap.keySet()) {
            outerNodes.add(outerNodesMap.get(nodeName));
        }

        packageNodes.addAll(jdkNodes);
        packageNodes.addAll(outerNodes);
    }


    private void buildNode(Node packageNode) {
        List<Node> classNodes = (List<Node>) packageNode.getNodeInfo();
        for (Node classNode : classNodes) {
            CtClass ctClass = (CtClass) classNode.getNodeInfo();
            try {
                ctClass.instrument(new PackageExprEditor(packageNode));
            } catch (CannotCompileException e) {
                logger.error("build the dependency of node("+ packageNode.getName() +") failed, err: " + e.getMessage());
            }

        }
    }

    private class PackageExprEditor extends ExprEditor {
        private Node srcNode;

        public PackageExprEditor(Node srcNode) {
            this.srcNode = srcNode;
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            String targetClassName = m.getClassName();
            if (!targetClassName.contains(".")) {
                return;
            }

            String targetPackageName = extractPackageFromClassName(targetClassName);
            Node targetNode = findNode(targetPackageName);

            if (targetNode == null && nodeManager.isProjectNode(targetPackageName)) { // 不是项目内筛选后的节点，但是是项目内的类（如内部类）
                targetNode = nodeManager.findNode(targetPackageName);
            } else if (targetNode == null) { // 若不是项目内的节点
                String from = targetClassName.startsWith("java.") ? "JDK" : "OUTER";
                if (from.equals("JDK")) {
                    if (jdkNodesMap.containsKey(targetPackageName)) {
                        targetNode = jdkNodesMap.get(targetPackageName);
                    } else {
                        CtClass jdkCtClass = manager.getCtClass(targetClassName);
                        targetNode = ctClass2PackageNode(jdkCtClass, targetPackageName, from);
                    }
                } else {
                    if (outerNodesMap.containsKey(targetPackageName)) {
                        targetNode = outerNodesMap.get(targetPackageName);
                    } else {
                        targetNode = ctClass2PackageNode(null, targetPackageName, from);
                    }
                }
            }

            if (filter(targetNode)) {
                srcNode.addEdge(srcNode.getName(), targetNode, "depend", 1);
                nodesMap.put(targetPackageName, targetNode);
                if (targetNode.getFrom().equals("JDK") && !jdkNodesMap.containsKey(targetPackageName)) {
                    jdkNodesMap.put(targetPackageName, targetNode);
                } else if (targetNode.getFrom().equals("OUTER") && !outerNodesMap.containsKey(targetPackageName)) {
                    outerNodesMap.put(targetPackageName, targetNode);
                }
            }


//            if (targetNode == null || targetPackageName.equals(srcNode.getName())) {
//                logger.info("the node(" + targetPackageName + ") of dependency is not exist");
//                return;
//            }
//            srcNode.addEdge(targetNode, "depend", 1);
        }
    }

    private Node ctClass2PackageNode(CtClass ctClass, String name, String from) {
        Node<List<Node>> packageNode = new Node<>();
        packageNode.setName(name);
        packageNode.setLevel(Level.PACKAGE);
        packageNode.setFrom(from);
        List<Node> classNodes = new ArrayList<>();
        classNodes.add(ctClass == null ? null : ctClass2ClassNode(ctClass));
        packageNode.setNodeInfo(classNodes);
        return packageNode;
    }

    private Node ctClass2ClassNode(CtClass ctClass) {
        Node<CtClass> classNode = new Node<>();
        classNode.setNodeInfo(ctClass);
        classNode.setLevel(Level.CLASS);
        classNode.setName(ctClass.getName());
        classNode.setFrom("PROJECT");
        return classNode;
    }

    private boolean filter(Node node) {
        for (Filter filter : filters) {
            if (filter.filter(node) == null) {
                return false;
            }
        }
        return true;
    }


    private String extractPackageFromClassName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }
}
