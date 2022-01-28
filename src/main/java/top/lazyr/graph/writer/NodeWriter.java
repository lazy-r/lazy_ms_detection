package top.lazyr.graph.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Node;
import top.lazyr.util.ExcelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/8
 */
public class NodeWriter extends Writer{
    private static Logger logger = LoggerFactory.getLogger(NodeWriter.class);
    private String workbookName;
    private String sheetName;

    public NodeWriter() {
        this("node_info.xlsx", "数据");
    }

    public NodeWriter(String workbookName, String sheetName) {
        this.workbookName = workbookName;
        this.sheetName = sheetName;
    }

    @Override
    public void write(List<Node> nodes) {
        List<List<String>> infos = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("节点");
        title.add("传入依赖数");
        title.add("传出依赖数");
        title.add("来源");
        title.add("粒度");
        infos.add(title);
        for (Node node : nodes) {
            List<String> info = new ArrayList<>();
            info.add(node.getName());
            info.add(node.getAfferentNum() + "");
            info.add(node.getEfferentNum() + "");
            info.add(node.getFrom());
            info.add(node.getLevel().stringValue());
            infos.add(info);
        }
        ExcelUtil.write2Excel(workbookName, sheetName, infos);
    }
}
