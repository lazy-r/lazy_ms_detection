package top.lazyr.graph.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.lazyr.graph.Graph;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.handler.FuncHandler;
import top.lazyr.graph.handler.Handler;
import top.lazyr.graph.writer.ConsoleWriter;
import top.lazyr.graph.writer.DependencyWriter;
import top.lazyr.graph.writer.NodeWriter;
import top.lazyr.graph.writer.Writer;
import top.lazyr.model.*;

import java.io.File;
import java.util.*;

/**
 * @author lazyr
 * @created 2021/12/10
 */
public class FZJSvcTransformer extends Transformer{
    private static Logger logger = LoggerFactory.getLogger(FZJSvcTransformer .class);
    private Map<String, Node> nodesMap;


    @Override
    public List<Node> transform(String projectPath) {
        File catalog = new File(projectPath);
        if (!catalog.exists()) {
            logger.info(projectPath + " not exist");
            return new ArrayList<>();
        }
        if (!catalog.isDirectory()) {
            logger.info(projectPath + " is not catalog");
            return new ArrayList<>();
        }

        File[] svcCatalogs = catalog.listFiles();
        List<Node> svcNodes = new ArrayList<>();

        for (File svcCatalog : svcCatalogs) {
            if (!svcCatalog.isDirectory()) {
                continue;
            }
            // 构建一个服务的func粒度的Node
            Graph funcGraph = buildFuncGraph(svcCatalog.getAbsolutePath());
            Node svcNode = buildSvcNode(svcCatalog.getName(), funcGraph);
            svcNodes.add(svcNode);
        }

        return svcNodes;
    }

    private Graph buildFuncGraph(String svcAbsolutePath) {
        List<Filter> filters = new ArrayList<>();
//        filters.add(new InnerClassFilter());
//        filters.add(new ProjectFilter());
        List<Writer> writers = new ArrayList<>();
        writers.add(new ConsoleWriter());
        writers.add(new DependencyWriter());
        writers.add(new NodeWriter());



        Handler handler = new FuncHandler();
        List<Filter> handlerFilters = new ArrayList<>();
//        handlerFilters.add(new InnerClassFilter());
//        handlerFilters.add(new ProjectFilter());
        handler.setFilters(handlerFilters);
        Graph graph = Graph.builder()
                .transformer(new FuncTransformer())
                .filters(filters)
                .handler(handler)
                .writers(writers)
                .build();

        graph.createGraph(svcAbsolutePath);
        return graph;
    }

    public Node buildSvcNode(String svcName, Graph funcGraph) {
        List<Node> nodes = funcGraph.getNodes();
        this.nodesMap = nodes2Map(nodes);

        Node<FZJSvcInfo> svcNode = new Node<>();

        List<FZJCallInfo> callInfos = new ArrayList<>();
        List<FZJApiInfo> apiInfos = new ArrayList<>();

        Set<CtClass> ctClasses = new HashSet<>();

        for (Node node : nodes) {
            CtClass ctClass = (CtClass) node.getNodeInfo();
            if (isFeign(ctClass)) { // 若为Feign接口
                String feignName = buildFeignName(ctClass);
                List<FZJCallInfo> nodeCassInfo = buildCallInfo(feignName, ctClass, node, funcGraph);
                callInfos.addAll(nodeCassInfo);
            }

            ctClasses.add(ctClass);
        }

        for (CtClass ctClass : ctClasses) {
            if (isAPI(ctClass)) {
                List<FZJApiInfo> nodeApiInfo = buildSvcAPI(ctClass);
                apiInfos.addAll(nodeApiInfo);
            }
        }

        FZJSvcInfo svcInfo = new FZJSvcInfo();
        svcInfo.setApiNames(apiInfos);
        svcInfo.setCalls(callInfos);
        svcInfo.setSvcName(svcName);
        svcInfo.setFuncGraph(funcGraph);
//        System.out.println("apiInfos => " + apiInfos);
//        System.out.println("callInfos => " + callInfos);


        svcNode.setNodeInfo(svcInfo);
        svcNode.setName(svcName);
        svcNode.setLevel(Level.SERVICE);
        svcNode.setFrom("PROJECT");

        return svcNode;
    }

    private List<FZJApiInfo> buildSvcAPI(CtClass ctClass) {
        boolean isAPI = isAPI(ctClass);
        String prefixPath = "";
        List<FZJApiInfo> apis = new ArrayList<>();
        if (isAPI) {
            prefixPath = buildPrefixPath(ctClass);
            apis.addAll(getFuncAPI(ctClass, prefixPath));
        }
        return apis;
    }

    private List<FZJApiInfo> getFuncAPI(CtClass ctClass, String prefixPath) {
        CtMethod[] methods = ctClass.getMethods();
        List<FZJApiInfo> apiInfos = new ArrayList<>();
        for (CtMethod m : methods) {
            FZJApiInfo apiInfo = new FZJApiInfo();
            RequestMapping requestMapping = null;
            GetMapping getMapping = null;
            PostMapping postMapping = null;
            PutMapping putMapping = null;
            DeleteMapping deleteMapping = null;
            PatchMapping patchMapping = null;
            try {
                requestMapping = (RequestMapping) m.getAnnotation(RequestMapping.class);
                getMapping = (GetMapping)m.getAnnotation(GetMapping.class);
                postMapping = (PostMapping)m.getAnnotation(PostMapping.class);
                putMapping = (PutMapping)m.getAnnotation(PutMapping.class);
                deleteMapping = (DeleteMapping)m.getAnnotation(DeleteMapping.class);
                patchMapping = (PatchMapping) m.getAnnotation(PatchMapping.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String path = "";
            String method = "";

            if (requestMapping != null) {
                RequestMethod rm = requestMapping.method()[0];
                if (rm == RequestMethod.GET) {
                    method = "GET";
                }
                if (rm == RequestMethod.PATCH) {
                    method = "PATCH";
                }
                if (rm == RequestMethod.PUT) {
                    method = "PUT";
                }
                if (rm == RequestMethod.DELETE) {
                    method = "DELETE";
                }
                if (rm == RequestMethod.POST) {
                    method = "POST";
                }
                if (rm == RequestMethod.OPTIONS) {
                    method = "OPTIONS";
                }
                if (rm == RequestMethod.TRACE) {
                    method = "TRACE";
                }
                if (rm == RequestMethod.HEAD) {
                    method = "HEAD";
                }
                path = requestMapping.value()[0];
            }
            if (getMapping != null) {
                method = "GET";
                path = (prefixPath + "/" + getMapping.value()[0]).replace("//", "/");
            }
            if (postMapping != null) {
                method = "POST";
                path = (prefixPath + "/" + postMapping.value()[0]).replace("//", "/");
            }
            if (putMapping != null) {
                method = "PUT";
                path = (prefixPath + "/" + putMapping.value()[0]).replace("//", "/");
            }
            if (deleteMapping != null) {
                path = "DELETE=>" + (prefixPath + "/" + deleteMapping.value()[0]).replace("//", "/");
            }
            if (patchMapping != null) {
                method = "PATCH";
                path = (prefixPath + "/" + deleteMapping.value()[0]).replace("//", "/");
            }
            if (path.equals("")) {
                continue;
            }
            apiInfo.setMethod(method);
            apiInfo.setPath(path);
            apiInfo.setType("HTTP");


            String funcName = extractMethodName(m.getLongName());
            List<String> params = extractParams(m.getLongName());
            apiInfo.setFuncName(funcName);
            apiInfo.setParams(params);
            try {
                apiInfo.setReturnName(m.getReturnType().getName());
            } catch (NotFoundException e) {
//                logger.error("set return name failed, err: " + e.getMessage());
                apiInfo.setReturnName(e.getMessage());
            }
            apiInfo.setClassName(extractClassName(m.getLongName()));
            apiInfos.add(apiInfo);
        }
        return apiInfos;
    }



    private List<FZJCallInfo> buildCallInfo(String srcName, CtClass ctClass, Node targetFuncNode, Graph funcGraph) {
        List<FZJCallInfo> callInfos = new ArrayList<>();

        for (CtMethod ctMethod : ctClass.getMethods()) {
            String methodName = ctClass.getName() + "." + extractMethodName(ctMethod.getLongName());
            if (targetFuncNode.getName().equals(methodName)) { // 若为node对应的方法
                RequestMapping requestMapping = null;
                GetMapping getMapping = null;
                PostMapping postMapping = null;
                PutMapping putMapping = null;
                DeleteMapping deleteMapping = null;
                PatchMapping patchMapping = null;
                try {
                    requestMapping = (RequestMapping) ctMethod.getAnnotation(RequestMapping.class);
                    getMapping = (GetMapping) ctMethod.getAnnotation(GetMapping.class);
                    postMapping = (PostMapping) ctMethod.getAnnotation(PostMapping.class);
                    putMapping = (PutMapping) ctMethod.getAnnotation(PutMapping.class);
                    deleteMapping = (DeleteMapping) ctMethod.getAnnotation(DeleteMapping.class);
                    patchMapping = (PatchMapping) ctMethod.getAnnotation(PatchMapping.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String path = "";
                String method = "";
                if (requestMapping != null) {
                    RequestMethod rm = requestMapping.method()[0];
                    if (rm == RequestMethod.GET) {
                        method = "GET";
                    }
                    if (rm == RequestMethod.PATCH) {
                        method = "PATCH";
                    }
                    if (rm == RequestMethod.PUT) {
                        method = "PUT";
                    }
                    if (rm == RequestMethod.DELETE) {
                        method = "DELETE";
                    }
                    if (rm == RequestMethod.POST) {
                        method = "POST";
                    }
                    if (rm == RequestMethod.OPTIONS) {
                        method = "OPTIONS";
                    }
                    if (rm == RequestMethod.TRACE) {
                        method = "TRACE";
                    }
                    if (rm == RequestMethod.HEAD) {
                        method = "HEAD";
                    }
                    path = requestMapping.value()[0];
                }
                if (getMapping != null) {
                    method = "GET";
                    path = getMapping.value()[0];
                }
                if (postMapping != null) {
                    method = "POST";
                    path = postMapping.value()[0];
                }
                if (putMapping != null) {
                    method = "PUT";
                    path = putMapping.value()[0];
                }
                if (deleteMapping != null) {
                    method = "DELETE";
                    path = deleteMapping.value()[0];
                }
                if (patchMapping != null) {
                    method = "PATCH";
                    path = patchMapping.value()[0];
                }

//                System.out.println(ctClass.getName() + " => path = " + path);
                if (path.equals("")) {
                    continue;
                }

                List<Node> srcNodes = funcGraph.getPreNodes(targetFuncNode);
//                System.out.println("srcNodes => " + srcNodes);
                for (Node srcNode : srcNodes) {
                    FZJCallInfo callInfo = new FZJCallInfo();
                    List<Edge> edges = srcNode.getEdges();
                    int weight = 0;
                    for (Edge edge : edges) {
                        if (edge.getTargetName().equals(targetFuncNode.getName())) {
                            weight = edge.getWeight();
                            break;
                        }
                    }
                    callInfo.setTargetAPIPath(path);
                    callInfo.setTargetServiceName(srcName);
                    callInfo.setWeight(weight);
                    callInfo.setTargetAPIMethod(method);


                    FZJApiInfo srcAPIInfo = new FZJApiInfo();
                    srcAPIInfo.setClassName(((CtClass)srcNode.getNodeInfo()).getName());
                    srcAPIInfo.setFuncName(srcNode.getName());

                    FZJApiInfo targetAPIInfo = new FZJApiInfo();
                    targetAPIInfo.setClassName(((CtClass)targetFuncNode.getNodeInfo()).getName());
                    targetAPIInfo.setFuncName(targetFuncNode.getName());

                    callInfo.setTargetApiInfo(targetAPIInfo);
                    callInfo.setSrcApiInfo(srcAPIInfo);
                    callInfos.add(callInfo);
                }
            }
        }
        return callInfos;
    }

    private boolean isFeign(CtClass ctClass) {
        if (ctClass == null) {
            return false;
        }

        FeignClient feignClient = null;

        try {
            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }

        return feignClient != null;
    }

    private boolean isAPI(CtClass ctClass) {
        if (ctClass == null) {
            return false;
        }

        Controller controller = null;
        RestController restController = null;
        ResponseBody responseBody = null;
        RequestMapping requestMapping = null;
        try {
            controller = (Controller)ctClass.getAnnotation(Controller.class);
            restController = (RestController)ctClass.getAnnotation(RestController.class);
            responseBody = (ResponseBody)ctClass.getAnnotation(ResponseBody.class);
            requestMapping = (RequestMapping)ctClass.getAnnotation(RequestMapping.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class api annotation failed: " + e.getMessage());
        }

        return controller != null || restController != null || responseBody != null || requestMapping != null;
    }

    protected Map<String, Node> nodes2Map(List<Node> classNodes) {
        Map<String, Node> nodesMap = new HashMap<>();
        for (Node classNode : classNodes) {
            nodesMap.put(classNode.getName(), classNode);
        }
        return nodesMap;
    }

    private String extractMethodName(String longMethodName) {
        String[] info = longMethodName.split("\\(");
        int lastIndex = info[0].lastIndexOf(".");
        String methodName = info[0].substring(lastIndex + 1);
        return methodName + "(" + info[1];
//        return info[0];
    }

    private String extractClassName(String longMethodName) {
        String[] info = longMethodName.split("\\(");
        int lastIndex = info[0].lastIndexOf(".");
        String className = info[0].substring(0, lastIndex);
        return className;
    }

    private List<String> extractParams(String longMethodName) {
//        System.out.println(longMethodName + ", ( => " + longMethodName.lastIndexOf("(") + ", len => " + (longMethodName.length() - 1));
        String paramStr = longMethodName.substring(longMethodName.lastIndexOf("(") + 1, longMethodName.length() - 1);
        String[] params = paramStr.split(",");
        List<String> paramList = new ArrayList<>();
        for (String param : params) {
            paramList.add(param);
        }
        return paramList;
    }

    private String buildPrefixPath(CtClass ctClass) {
        RestController restController = null;
        RequestMapping requestMapping = null;
        String prefixPath = "";
        try {
            restController = (RestController)ctClass.getAnnotation(RestController.class);
            requestMapping = (RequestMapping)ctClass.getAnnotation(RequestMapping.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class api annotation failed: " + e.getMessage());
        }
        if (restController != null) {
            prefixPath = restController.value();
//            System.out.println(prefixPath);
        }

        if (requestMapping != null) {
            String[] prefixValues = requestMapping.value();
            if (prefixValues.length != 0) {
                prefixPath = prefixValues[0];
            }
            String[] prefixPaths = requestMapping.path();
            if (prefixPaths.length != 0) {
                prefixPath = prefixPaths[0];
            }
        }
        return prefixPath;

    }

    private String buildFeignName(CtClass ctClass) {
        FeignClient feignClient = null;
        String feignName = "";



        try {
            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }
        if (feignClient == null) {
            return "";
        }
        feignName = feignClient.value();
        if (feignName.equals("")) {
            feignName = feignClient.name();
        }
        return feignName;
    }

}
