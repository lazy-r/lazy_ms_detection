package top.lazyr.graph.writer;

import top.lazyr.model.FZJCallInfo;
import top.lazyr.model.FZJSvcInfo;
import top.lazyr.model.Node;
import top.lazyr.util.ExcelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/10
 */
public class FZJDependencyWriter extends Writer{
    private String fileName;

    public FZJDependencyWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void write(List<Node> nodes) {
        List<List<String>> infos = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("节点");
        title.add("节点的方法");
        title.add("被依赖节点");
        title.add("被依赖节点的方法");
        title.add("权重");
        infos.add(title);
        for (Node node : nodes) {
            FZJSvcInfo svcInfo = (FZJSvcInfo) node.getNodeInfo();
            List<FZJCallInfo> calls = svcInfo.getCalls();
            for (FZJCallInfo call : calls) {
                List<String> info = new ArrayList<>();
                info.add(node.getName());
                info.add(call.getSrcApiInfo().getFuncName());
                info.add(call.getTargetServiceName());
                info.add(call.getTargetApiInfo().getFuncName());
                info.add(call.getWeight() + "");
                infos.add(info);
            }
        }
//        ExcelUtil.write2Excel("fzj_2.xlsx", "数据", infos);
        ExcelUtil.write2Excel(fileName, "数据", infos);
    }
}
