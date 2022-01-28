package top.lazyr.graph.transformer;

import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Level;
import top.lazyr.model.Node;
import top.lazyr.util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/11/21
 */

public class PackageTransformer extends Transformer {
    private static Logger logger = LoggerFactory.getLogger(PackageTransformer.class);


    public PackageTransformer() {
        super();
    }


    @Override
    public List<Node> transform(String projectPath) {
        List<Node> packageNodes = new ArrayList<>();
        Map<String, List<CtClass>> packageMap = new HashMap<>();
        List<String> filesAbsolutePath = FileUtil.getFilesAbsolutePath(projectPath, ".class");

        for (String fileAbsolutePath : filesAbsolutePath) { // 构建package => [class 1、class 2、... 、class n]的映射
            CtClass ctClass = this.manager.getOuterCtClass(fileAbsolutePath);
            List<CtClass> ctClasses = packageMap.getOrDefault(ctClass.getPackageName(), new ArrayList<>());
            ctClasses.add(ctClass);
            packageMap.put(ctClass.getPackageName(), ctClasses);
        }

        for (String packageName : packageMap.keySet()) { // 遍历包中类并构建Node
            Node<List<Node>> packageNode = new Node<>();
            packageNode.setName(packageName);
            packageNode.setLevel(Level.PACKAGE);
            packageNode.setFrom("PROJECT");

            List<Node> classNodes = new ArrayList<>();
            for (CtClass ctClass : packageMap.get(packageName)) {
                classNodes.add(ctClass2ClassNode(ctClass));
            }
            packageNode.setNodeInfo(classNodes);
            packageNodes.add(packageNode);
        }

        return packageNodes;
    }


    private Node ctClass2ClassNode(CtClass ctClass) {
        Node<CtClass> classNode = new Node<>();
        classNode.setNodeInfo(ctClass);
        classNode.setLevel(Level.CLASS);
        classNode.setName(ctClass.getName());
        classNode.setFrom("PROJECT");
        return classNode;
    }

}
