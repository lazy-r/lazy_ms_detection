package top.lazyr.graph.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.Main;
import top.lazyr.model.Level;
import top.lazyr.model.Node;
import top.lazyr.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class FuncTransformer extends Transformer{
    private static Logger logger = LoggerFactory.getLogger(FuncTransformer.class);

    public FuncTransformer() {
        super();
    }


    @Override
    public List<Node> transform(String projectPath) {
        List<String> filesAbsolutePath = FileUtil.getFilesAbsolutePath(projectPath, ".class");
        List<Node> funcNodes = new ArrayList<>();
        for (String fileAbsolutePath : filesAbsolutePath) {
            CtClass ctClass = this.manager.getOuterCtClass(fileAbsolutePath);
            funcNodes.addAll(ctClass2FuncNodes(ctClass));
        }
        return funcNodes;
    }


    // TODO: 代优化，取消重复代码
    private List<Node> ctClass2FuncNodes(CtClass ctClass) {
        List<Node> funcNodes = new ArrayList<>();
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            Node<CtClass> funcNode = new Node<>();
            funcNode.setNodeInfo(ctClass);
            // 方法名为 当前类名 + 方法名 （忽略父类）
            funcNode.setName(ctClass.getName() + "." + extractMethodName(method.getLongName()));
            funcNode.setFrom("PROJECT");
            funcNode.setLevel(Level.FUNC);
            funcNodes.add(funcNode);
        }
        return funcNodes;
    }


    private String extractMethodName(String longMethodName) {
        String[] info = longMethodName.split("\\(");
        int lastIndex = info[0].lastIndexOf(".");
        String methodName = info[0].substring(lastIndex + 1);
        return methodName + "(" + info[1];
    }

}
