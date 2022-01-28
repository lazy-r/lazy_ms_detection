package top.lazyr.graph.handler;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
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

import java.util.*;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class FuncHandler extends Handler{
    private static Logger logger = LoggerFactory.getLogger(FuncHandler.class);
    private Map<String, Node> jdkFuncNodesMap = new HashMap<>();
    private Map<String, Node> outerFuncNodesMap = new HashMap<>();
    private CtClassManager manager = CtClassManager.getCtClassManager();
    private ProjectNodeManager nodeManager;


    @Override
    public void handle(List<Node> funcNodes, ProjectNodeManager nodeManager) {
        this.nodesMap = nodes2Map(funcNodes);
        this.nodeManager = nodeManager;
        Set<CtClass> set = new HashSet<>();
        for (Node node : funcNodes) {
            set.add((CtClass) node.getNodeInfo());
        }
        for (CtClass ctClass : set) {
            buildNodes(ctClass);
        }

        List<Node> jdkFuncNodes = new ArrayList<>();
        List<Node> outerFuncNodes = new ArrayList<>();

        for (String nodeName : jdkFuncNodesMap.keySet()) {
            jdkFuncNodes.add(jdkFuncNodesMap.get(nodeName));
        }
        for (String nodeName : outerFuncNodesMap.keySet()) {
            outerFuncNodes.add(outerFuncNodesMap.get(nodeName));
        }

        funcNodes.addAll(jdkFuncNodes);
        funcNodes.addAll(outerFuncNodes);


    }

    private void buildNodes(CtClass ctClass) {
        for (CtMethod method : ctClass.getMethods()) {
            Node srcFuncNode = findNode(method.getLongName());
            if (srcFuncNode == null) {
                continue;
            }
            try {
                method.instrument(new FuncExprEditor(srcFuncNode));
            } catch (CannotCompileException e) {
                logger.error("build the dependency of node("+ srcFuncNode.getName() +") failed, err: " + e.getMessage());
            }
        }
    }

    private class FuncExprEditor extends ExprEditor {
        private Node srcNode;

        public FuncExprEditor(Node srcNode) {
            this.srcNode = srcNode;
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            try {
                // 方法所在的类名.方法名(方法参数列表) 而不是方法的父类
                String targetFuncName = m.getClassName() + "." + extractMethodName(m.getMethod().getLongName());
                Node targetFuncNode = findNode(targetFuncName);


                if (targetFuncNode == null && nodeManager.isProjectNode(targetFuncName)) { // 不为项目内筛选后的节点，但是是项目内的类（如内部类）
                    targetFuncNode = nodeManager.findNode(targetFuncName);
//                    logger.info("the node(" + targetFuncName + ") of dependency is not exist");
//                    return;
                } else if (targetFuncNode == null) { // 若不为项目内的节点
                    String from = targetFuncName.startsWith("java.") ? "JDK" : "OUTER";


                    if (from.equals("JDK")) {
                        if (jdkFuncNodesMap.containsKey(targetFuncName)) {
                            targetFuncNode = jdkFuncNodesMap.get(targetFuncName);
                        } else {
                            CtClass jdkCtClass = manager.getCtClass(m.getClassName());
                            targetFuncNode = ctClass2FuncNodes(jdkCtClass, targetFuncName, from);
                        }
                    } else {
                        if (outerFuncNodesMap.containsKey(targetFuncName)) {
                            targetFuncNode = outerFuncNodesMap.get(targetFuncName);
                        } else {
                            targetFuncNode = ctClass2FuncNodes(null, targetFuncName, from);
                        }
                    }
                }

                if (filter(targetFuncNode)) {
                    srcNode.addEdge(srcNode.getName(), targetFuncNode, "depend", 1);
                    nodesMap.put(targetFuncName, targetFuncNode);
                    if (targetFuncNode.getFrom().equals("JDK") && !jdkFuncNodesMap.containsKey(targetFuncName)) {
                        jdkFuncNodesMap.put(targetFuncName, targetFuncNode);
                    } else if (targetFuncNode.getFrom().equals("OUTER") && !outerFuncNodesMap.containsKey(targetFuncName)) {
                        outerFuncNodesMap.put(targetFuncName, targetFuncNode);
                    }
                }

            } catch (NotFoundException e) {
//                logger.info("build the method call of node("+ srcNode.getName() +") failed, err: " + e.getMessage());
            }

        }
    }


    private Node ctClass2FuncNodes(CtClass ctClass, String name, String from) {
        Node<CtClass> funcNode = new Node<>();
        funcNode.setNodeInfo(ctClass);
        funcNode.setLevel(Level.FUNC);
        funcNode.setName(name);
        funcNode.setFrom(from);
        return funcNode;
    }


    private boolean filter(Node node) {
        for (Filter filter : filters) {
            if (filter.filter(node) == null) {
                return false;
            }
        }
        return true;
    }

    private String extractMethodName(String longMethodName) {
        String[] info = longMethodName.split("\\(");
        int lastIndex = info[0].lastIndexOf(".");
        String methodName = info[0].substring(lastIndex + 1);
        return methodName + "(" + info[1];
    }
}
