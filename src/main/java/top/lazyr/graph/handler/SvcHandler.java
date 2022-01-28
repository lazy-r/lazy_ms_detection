package top.lazyr.graph.handler;

import javassist.CannotCompileException;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.graph.ProjectNodeManager;
import top.lazyr.model.CallInfo;
import top.lazyr.model.Node;
import top.lazyr.model.SvcInfo;

import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/12/1
 */
public class SvcHandler extends Handler{
    private static Logger logger = LoggerFactory.getLogger(SvcHandler.class);

    @Override
    public void handle(List<Node> svcNodes, ProjectNodeManager nodeManager) {
        this.nodesMap = nodes2Map(svcNodes);
        for (Node svcNode: svcNodes) {
            buildNode(svcNode);
        }
    }


    private void buildNode(Node svcNode) {
        SvcInfo svcInfo = (SvcInfo) svcNode.getNodeInfo();
        for (CallInfo call : svcInfo.getCalls()) {
            Node callSvcNode = findNode(call.getTargetServiceName());
            if (callSvcNode != null && call.getWeight() > 0 /* && !callSvcNode.getName().equals(svcNode.getName()) */) {
//                System.out.println(svcInfo.getSvcName() + " => " + call.getTargetServiceName() + " : " + call.getTargetAPIPath() + "(" + call.getWeight() + ")");
//                svcNode.addEdge(svcNode.getName(), callSvcNode, "depend", call.getWeight());
                svcNode.addEdge(svcNode.getName(), callSvcNode, "depend", 1);
            } else {
                logger.info("the node(" + call.getTargetServiceName() + ") of dependency is not exist");
            }
        }
    }
}
