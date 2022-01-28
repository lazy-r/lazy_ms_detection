package top.lazyr.graph.writer;

import top.lazyr.model.FZJApiInfo;
import top.lazyr.model.FZJSvcInfo;
import top.lazyr.model.Node;
import top.lazyr.util.ExcelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/10
 */
public class FZJSvcInfoWriter extends Writer{
    private String fileName;

    public FZJSvcInfoWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void write(List<Node> nodes) {
        List<List<String>> infos = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("服务");
        title.add("接口");
        title.add("参数类型");
        title.add("返回值");
        infos.add(title);

        for (Node node : nodes) {

            FZJSvcInfo svcInfo = (FZJSvcInfo) node.getNodeInfo();
            List<FZJApiInfo> apiInfos = svcInfo.getApiNames();
            for (FZJApiInfo apiInfo : apiInfos) {
                List<String> info = new ArrayList<>();
                info.add(node.getName());
                info.add(apiInfo.getClassName() + "." + apiInfo.getFuncName());
                info.add(apiInfo.getParams().toString().replace("[", "").replace("]", "").replace(",", "\n").replace(" ", ""));
                info.add(apiInfo.getReturnName());
                infos.add(info);
            }
        }

        ExcelUtil.write2Excel(fileName, "数据", infos);
    }
}
