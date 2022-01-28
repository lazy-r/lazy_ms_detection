package top.lazyr.graph.filter;

import cn.hutool.core.util.StrUtil;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Level;
import top.lazyr.model.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class ProjectFilter implements Filter{
    private static Logger logger = LoggerFactory.getLogger(ProjectFilter.class);

    @Override
    public List<Node> filter(List<Node> nodes) {
        if (nodes == null || nodes.size() == 0) {
            logger.info("filter nodes that's size is empty.");
            return nodes;
        }

        List<Node> filterNodes = null;
        int level = nodes.get(0).getLevel().intValue();

        switch (level){
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
        Node filterNode = null;
        int level = node.getLevel().intValue();
        switch (level){
            case Level.PACKAGE_LEVEL:
                filterNode = node;
                break;
            case Level.CLASS_LEVEL:
                filterNode = filterFuncNode(node);
                break;
            case Level.FUNC_LEVEL:
                filterNode = filterFuncNode(node);
                break;
            default:
                filterNode = node;
                logger.info("no filter, because node's level(" + level +") is not exist.");
        }
        return filterNode;
    }

    private List<Node> filterFuncNodes(List<Node> nodes) {
        List<Node> filterNodes = new ArrayList<>();
//        Set<String> names = new HashSet<>();
//        for (Node node : nodes) {
//            CtClass ctClass = (CtClass) node.getNodeInfo();
//            names.add(ctClass.getName());
//        }
//        String prefix = longestCommonPrefix(names);

        for (Node node : nodes) {
            Node filterNode = filterFuncNode(node);
            if (filterNode != null) {
                filterNodes.add(filterNode);
            }
        }

        return filterNodes;
    }


    private Node filterFuncNode(Node node) {
        Object nodeInfo = node.getNodeInfo();
        if (nodeInfo == null) { // node为外部依赖
            return null;
        }
        if (node.getName().startsWith("java.")) { // node为原生JDK依赖
            return null;
        }

        return node;
    }



    private String longestCommonPrefix(Set<String> strs) {
        return longestCommonPrefix(strs.toArray(new String[strs.size()]));
    }

    private String longestCommonPrefix(String[] strs) {
        // 参数校验
        if (strs == null || strs.length == 0) {
            return "";
        }
        // 获取字符串长度最小值
        int minLenth = Integer.MAX_VALUE;
        for (int i = 0; i < strs.length; i++) {
            if (strs[i].length() < minLenth) {
                minLenth = strs[i].length();
            }
        }

        int index = 0;
        boolean flag = true;
        // 比较所有字符串第i个字母
        for (index = 0; index < minLenth; index++) {
            char ch = strs[0].charAt(index);
            // 内循环比较每个字符串指定位置是否相等
            for (int j = 1; j < strs.length; j++) {
                if (strs[j].charAt(index) != ch) {
                    flag = false;
                    break;
                }
            }

            if (flag == false) {
                break;
            }
        }

        return strs[0].substring(0, index);
    }


}
