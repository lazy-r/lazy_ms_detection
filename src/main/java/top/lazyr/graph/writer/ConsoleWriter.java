package top.lazyr.graph.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Edge;
import top.lazyr.model.Node;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
public class ConsoleWriter extends Writer {
    private static Logger logger = LoggerFactory.getLogger(ConsoleWriter.class);

    @Override
    public void write(List<Node> nodes) {
        if (nodes.size() == 0) {
            logger.info("ConsoleWriter: write nodes that's size is zero.");
            return;
        }

        System.out.println("====================" + nodes.get(0).getLevel().stringValue() + "====================");

        for (Node node : nodes) {
            System.out.println(node.getName() + " => afferent: " + node.getAfferentNum() + ", efferent: " + node.getEfferentNum() );
            List<Edge> edges = node.getEdges();
            if (edges == null) {
                System.out.print("****************************************************");
                System.out.println("****************************************************");
                continue;
            }
            for (Edge edge : edges) {
                switch (edge.getType()) {
                    case "depend":
                        System.out.print("\t\t\t——" + edge.getWeight() + "——> ");
                        break;
                    case "implements":
                        System.out.print("\t\t\t--" + edge.getWeight() + "--> ");
                        break;
                    case "extends":
                        System.out.print("\t\t\t==" + edge.getWeight() + "==> ");
                        break;
                }
                System.out.println(edge.getTargetName());
            }
            System.out.print("****************************************************");
            System.out.println("****************************************************");
        }
    }
}
