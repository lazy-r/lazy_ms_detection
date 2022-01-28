package top.lazyr.graph.transformer;

import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Level;
import top.lazyr.model.Node;
import top.lazyr.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class ClassTransformer extends Transformer {
    private static Logger logger = LoggerFactory.getLogger(PackageTransformer.class);


    public ClassTransformer() {
        super();
    }

    @Override
    public List<Node> transform(String projectPath) {
        List<Node> classNodes = new ArrayList<>();
        List<String> filesAbsolutePath = FileUtil.getFilesAbsolutePath(projectPath, ".class");
        for (String fileAbsolutePath : filesAbsolutePath) {
            CtClass ctClass = manager.getOuterCtClass(fileAbsolutePath);
            Node<CtClass> classNode = ctClass2ClassNode(ctClass);
            classNodes.add(classNode);
        }

        return classNodes;
    }

    // TODO: 代优化，取消重复代码
    private Node<CtClass> ctClass2ClassNode(CtClass ctClass) {
        Node<CtClass> classNode = new Node<>();
        classNode.setNodeInfo(ctClass);
        classNode.setLevel(Level.CLASS);
        classNode.setName(ctClass.getName());
        classNode.setFrom("PROJECT");
        return classNode;
    }
}
