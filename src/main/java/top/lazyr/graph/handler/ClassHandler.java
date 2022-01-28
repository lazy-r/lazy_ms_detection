package top.lazyr.graph.handler;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
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
 * @created 2021/11/22
 */
public class ClassHandler extends Handler{
    private static Logger  logger = LoggerFactory.getLogger(ClassHandler.class);
    private Map<String, Node> jdkNodesMap = new HashMap<>();
    private Map<String, Node> outerNodesMap = new HashMap<>();
    private CtClassManager manager = CtClassManager.getCtClassManager();
    private ProjectNodeManager nodeManager;


    @Override
    public void handle(List<Node> classNodes, ProjectNodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.nodesMap = nodes2Map(classNodes);
        for (Node classNode : classNodes) {
            buildNode(classNode);
        }
        List<Node> jdkNodes = new ArrayList<>();
        List<Node> outerNodes = new ArrayList<>();

        for (String nodeName : jdkNodesMap.keySet()) {
            jdkNodes.add(jdkNodesMap.get(nodeName));
        }
        for (String nodeName : outerNodesMap.keySet()) {
            outerNodes.add(outerNodesMap.get(nodeName));
        }

        classNodes.addAll(jdkNodes);
        classNodes.addAll(outerNodes);

    }

    private void buildNode(Node classNode) {
        buildDepend(classNode);
//        buildExtends(classNode);
//        buildImplements(classNode);
    }

    private void buildExtends(Node classNode) {
        CtClass ctClass = (CtClass) classNode.getNodeInfo();
        try {
            CtClass superclass = ctClass.getSuperclass();
            String superclassName = superclass.getName();
            Node superNode = findNode(superclassName);
            if (superNode == null) {
                return;
            }
            classNode.addEdge(classNode.getName(), superNode, "extends", 200);
        } catch (NotFoundException e) {
            logger.error("build the parent of node("+ classNode.getName() +") failed, err: " + e.getMessage());
        }
    }

    private void buildImplements(Node classNode) {
        CtClass ctClass = (CtClass) classNode.getNodeInfo();
        try {
            CtClass[] interfacesCtClass = ctClass.getInterfaces();
            for (CtClass interfaceCtClass : interfacesCtClass) {
                Node interfaceNode = findNode(interfaceCtClass.getName());
                if (interfaceNode == null) {
                    continue;
                }
                classNode.addEdge(classNode.getName(), interfaceNode, "implements", 100);
            }
        } catch (NotFoundException e) {
            logger.error("build the interfaces of node("+ classNode.getName() +") failed, err: " + e.getMessage());
        }

    }

    private void buildDepend(Node classNode) {
        CtClass ctClass = (CtClass) classNode.getNodeInfo();
        try {
            ctClass.instrument(new ClassExprEditor(classNode));
        } catch (CannotCompileException e) {
            logger.error("build the dependency of node("+ classNode.getName() +") failed, err: " + e.getMessage());
        }
    }

    private class ClassExprEditor extends ExprEditor {
        private Node srcNode;

        public ClassExprEditor(Node srcNode) {
            this.srcNode = srcNode;
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            String targetClassName = m.getClassName();
            Node targetNode = findNode(targetClassName);


            if (targetNode == null && nodeManager.isProjectNode(targetClassName)) { // 不是项目内筛选后的节点，但是是项目内的类（如内部类）
                targetNode = nodeManager.findNode(targetClassName);
            } else if (targetNode == null) { // 不是项目内的节点
                String from = targetClassName.startsWith("java.") ? "JDK" : "OUTER";
                if (from.equals("JDK")) {
                    if (jdkNodesMap.containsKey(targetClassName)) {
                        targetNode = jdkNodesMap.get(targetClassName);
                    } else {
                        CtClass jdkCtClass = manager.getCtClass(targetClassName);
                        targetNode = ctClass2ClassNode(jdkCtClass, targetClassName, from);
                    }
                } else {
                    if (outerNodesMap.containsKey(targetClassName)) {
                        targetNode = outerNodesMap.get(targetClassName);
                    } else {
                        targetNode = ctClass2ClassNode(null, targetClassName, from);
                    }
                }
            }

            if (filter(targetNode)) {
                srcNode.addEdge(srcNode.getName(), targetNode, "depend", 1);
                nodesMap.put(targetClassName, targetNode);
                if (targetNode.getFrom().equals("JDK") && !jdkNodesMap.containsKey(targetClassName)) {
                    jdkNodesMap.put(targetClassName, targetNode);
                } else if (targetNode.getFrom().equals("OUTER") && !outerNodesMap.containsKey(targetClassName)) {
                    outerNodesMap.put(targetClassName, targetNode);
                }
            }
        }
    }

    private boolean filter(Node node) {
        for (Filter filter : filters) {
            if (filter.filter(node) == null) {
                return false;
            }
        }
        return true;
    }

    private Node<CtClass> ctClass2ClassNode(CtClass ctClass, String name, String from) {
        Node<CtClass> classNode = new Node<>();
        classNode.setNodeInfo(ctClass);
        classNode.setLevel(Level.CLASS);
        classNode.setName(name);
        classNode.setFrom(from);
        return classNode;
    }
}
