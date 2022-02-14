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
 * @created 2021/12/25
 */
public class ZXMSWriter extends Writer{
    private static Logger logger = LoggerFactory.getLogger(ZXMSWriter.class);
    private String workbookName;
    private String sheetName;

    public ZXMSWriter() {
        this("dop.xlsx", "数据");
    }

    public ZXMSWriter(String workbookName) {
        this(workbookName, "数据");
    }

    public ZXMSWriter(String workbookName, String sheetName) {
        this.workbookName = workbookName;
        this.sheetName = sheetName;
    }

    @Override
    public void write(List<Node> nodes) {
        List<List<String>> infos = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("sourceNode");
        title.add("targetNode");
        title.add("value");
        infos.add(title);
        for (Node node : nodes) {
            List<Edge> edges = node.getEdges();
            if (edges == null) {
//                List<String> info = new ArrayList<>();
//                info.add(node.getName());
//                info.add("");
//                info.add("0");
//                info.add("");
//                info.add(node.getLevel().stringValue());
//                infos.add(info);
                continue;
            }
            for (Edge edge : edges) {
                List<String> info = new ArrayList<>();
                info.add(node.getName());
                info.add(edge.getTargetName());
                info.add(edge.getWeight() + "");
//                info.add(edge.getType());
//                info.add(node.getLevel().stringValue());
                infos.add(info);
            }
        }
        ExcelUtil.write2Excel(workbookName, sheetName, infos);
    }
}
