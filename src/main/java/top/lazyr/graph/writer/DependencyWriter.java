package top.lazyr.graph.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Edge;
import top.lazyr.model.Node;
import top.lazyr.util.ExcelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/8
 */
public class DependencyWriter extends Writer{
    private static Logger logger = LoggerFactory.getLogger(DependencyWriter.class);
    private String workbookName;
    private String sheetName;

    public DependencyWriter() {
        this("dependency.xlsx", "数据");
    }

    public DependencyWriter(String workbookName, String sheetName) {
        this.workbookName = workbookName;
        this.sheetName = sheetName;
    }

    @Override
    public void write(List<Node> nodes) {
        List<List<String>> infos = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("节点");
        title.add("依赖节点");
        title.add("权重");
        title.add("边类型");
        title.add("粒度");
        infos.add(title);
        for (Node node : nodes) {
            List<Edge> edges = node.getEdges();
            if (edges == null) {
                List<String> info = new ArrayList<>();
                info.add(node.getName());
                info.add("");
                info.add("0");
                info.add("");
                info.add(node.getLevel().stringValue());
                infos.add(info);
                continue;
            }
            for (Edge edge : edges) {
                List<String> info = new ArrayList<>();
                info.add(node.getName());
                info.add(edge.getTargetName());
                info.add(edge.getWeight() + "");
                info.add(edge.getType());
                info.add(node.getLevel().stringValue());
                infos.add(info);
            }
        }
        ExcelUtil.write2Excel(workbookName, sheetName, infos);
    }
}
