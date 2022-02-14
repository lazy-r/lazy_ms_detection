package top.lazyr.graph.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.graph.Graph;
import top.lazyr.graph.ProjectNodeManager;
import top.lazyr.model.*;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/10
 */
public class FZJSvcHandler extends Handler{
    private static Logger logger = LoggerFactory.getLogger(FZJSvcHandler.class);
    private ProjectNodeManager nodeManager;

    @Override
    public void handle(List<Node> svcNodes, ProjectNodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.nodesMap = nodes2Map(svcNodes);
        for (Node svcNode : svcNodes) {
            buildNode(svcNode);
        }
    }


    private void buildNode(Node srcNode) {
        FZJSvcInfo svcInfo = (FZJSvcInfo) srcNode.getNodeInfo();
        Graph funcGraph = svcInfo.getFuncGraph();
        List<FZJCallInfo> calls = svcInfo.getCalls();
        for (FZJCallInfo call : calls) {
            Node targetNode = findNode(call.getTargetServiceName());
            if (targetNode != null && call.getWeight() > 0 /* && !callSvcNode.getName().equals(svcNode.getName()) */) {
                srcNode.addEdge(srcNode.getName(), targetNode, "depend", call.getWeight());
            } else {
                logger.info("the node(" + call.getTargetServiceName() + ") of dependency is not exist");
            }
        }
    }
}
