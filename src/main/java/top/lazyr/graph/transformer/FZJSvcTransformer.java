package top.lazyr.graph.transformer;

import cn.hutool.core.util.StrUtil;
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
            // 以目录名为微服务名
            Node svcNode = buildSvcNode(svcCatalog.getName(), funcGraph);
            svcNodes.add(svcNode);
        }

        return svcNodes;
    }

    private Graph buildFuncGraph(String svcAbsolutePath) {
        List<Filter> filters = new ArrayList<>();
        List<Writer> writers = new ArrayList<>();

        Handler handler = new FuncHandler();
        List<Filter> handlerFilters = new ArrayList<>();
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

    /**
     * 构建一个服务粒度的Node，这个Node记录如下信息
     * - 对外暴露的接口，controller层（RequestMapping、GetMapping、...）
     * - 调用关系，FeignClient数据
     * @param svcName
     * @param funcGraph
     * @return
     */
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
                String feignName = getFeignName(ctClass);
                String feignPrefix = getFeignPrefix(ctClass);
                // 获取svcName调用的其他服务
                List<FZJCallInfo> nodeCassInfo = buildCallInfo(feignName, feignPrefix, ctClass, node, funcGraph);
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


        svcNode.setNodeInfo(svcInfo);
        svcNode.setName(svcName);
        svcNode.setLevel(Level.SERVICE);
        svcNode.setFrom("PROJECT");

        return svcNode;
    }

    /**
     * 若ctClass有@FeignClient注解，则返回注解中path属性的值
     * 若ctClass无@FeignClient注解，则返回""
     * @param ctClass
     * @return
     */
    private String getFeignPrefix(CtClass ctClass) {
        FeignClient feignClient = null;
        String prefix = "";
        try {
            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }
        if (feignClient == null) { // 若该类无FeignClient注解
            return "";
        }
        prefix = feignClient.path();
        return prefix;
    }

    /**
     *
     * @param ctClass
     * @return
     */
    private List<FZJApiInfo> buildSvcAPI(CtClass ctClass) {
        boolean isAPI = isAPI(ctClass);
        String prefixPath = "";
        List<FZJApiInfo> apis = new ArrayList<>();
        if (isAPI) {
            prefixPath = buildControllerPrefix(ctClass);
            apis.addAll(getFuncAPI(ctClass, prefixPath));
        }
        return apis;
    }

    private List<FZJApiInfo> getFuncAPI(CtClass ctClass, String prefixPath) {
        CtMethod[] ctMethods = ctClass.getMethods();
        List<FZJApiInfo> apiInfos = new ArrayList<>();
        for (CtMethod ctMethod : ctMethods) {
            FZJApiInfo apiInfo = new FZJApiInfo();
            RequestMapping requestMapping = null;
            GetMapping getMapping = null;
            PostMapping postMapping = null;
            PutMapping putMapping = null;
            DeleteMapping deleteMapping = null;
            PatchMapping patchMapping = null;
            try {
                requestMapping = (RequestMapping) ctMethod.getAnnotation(RequestMapping.class);
                getMapping = (GetMapping)ctMethod.getAnnotation(GetMapping.class);
                postMapping = (PostMapping)ctMethod.getAnnotation(PostMapping.class);
                putMapping = (PutMapping)ctMethod.getAnnotation(PutMapping.class);
                deleteMapping = (DeleteMapping)ctMethod.getAnnotation(DeleteMapping.class);
                patchMapping = (PatchMapping) ctMethod.getAnnotation(PatchMapping.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String path = "";
            Set<String> methods = new HashSet<>();
            /**
             * 若为RequestMapping
             *  method属性可以不设置，也可以设置多个
             *  value属性也可以不设置
             */
            if (requestMapping != null) {
                methods = getMethodsFromRequestMapping(requestMapping);
                path = buildPath(prefixPath, requestMapping.value());
            }
            if (getMapping != null) {
                methods.add("GET");
                path = buildPath(prefixPath, getMapping.value());
            }
            if (postMapping != null) {
                methods.add("POST");
                path = buildPath(prefixPath, postMapping.value());
            }
            if (putMapping != null) {
                methods.add("PUT");
                path = buildPath(prefixPath, putMapping.value());
            }
            if (deleteMapping != null) {
                methods.add("DELETE");
                path = buildPath(prefixPath, deleteMapping.value());
            }
            if (patchMapping != null) {
                methods.add("PATCH");
                path = buildPath(prefixPath, patchMapping.value());
            }
            if (path.equals("")) {
                continue;
            }
            apiInfo.setMethods(methods);
            apiInfo.setPath(path);
            apiInfo.setType("HTTP");


            String funcName = extractMethodName(ctMethod.getLongName());
            List<String> params = extractParams(ctMethod.getLongName());
            apiInfo.setFuncName(funcName);
            apiInfo.setParams(params);
            try {
                apiInfo.setReturnName(ctMethod.getReturnType().getName());
            } catch (NotFoundException e) {
//                logger.error("set return name failed, err: " + e.getMessage());
                apiInfo.setReturnName(e.getMessage());
            }
            apiInfo.setClassName(extractClassName(ctMethod.getLongName()));
            apiInfos.add(apiInfo);
        }
        return apiInfos;
    }


    private List<FZJCallInfo> buildCallInfo(String srcName, String feignPrefix, CtClass ctClass, Node targetFuncNode, Graph funcGraph) {
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
                Set<String> methods = new HashSet<>();
                if (requestMapping != null) {
                    methods = getMethodsFromRequestMapping(requestMapping);
                    path = buildPath(feignPrefix, requestMapping.value());
                }
                if (getMapping != null) {
                    methods.add("GET");
                    path = buildPath(feignPrefix, getMapping.value());
                }
                if (postMapping != null) {
                    methods.add("POST");
                    path = buildPath(feignPrefix, postMapping.value());
                }
                if (putMapping != null) {
                    methods.add("PUT");
                    path = buildPath(feignPrefix, putMapping.value());
                }
                if (deleteMapping != null) {
                    methods.add("DELETE");
                    path = buildPath(feignPrefix, deleteMapping.value());
                }
                if (patchMapping != null) {
                    methods.add("PATCH");
                    path = buildPath(feignPrefix, patchMapping.value());
                }

                if (path.equals("")) {
                    continue;
                }

                List<Node> srcNodes = funcGraph.getPreNodes(targetFuncNode);
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
                    callInfo.setTargetAPIMethods(methods);


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

    /**
     * prefix = a, values[0] = b, 返回 /a/b
     * prefix = a, values[0] = b/, 返回 /a/b
     * prefix = /a, values[0] = /b, 返回 /a/b
     * prefix = , values[0] = b, 返回 b
     * prefix = , values[0] = , 返回 /
     *
     * @param prefixPath
     * @param values
     * @return
     */
    private String buildPath(String prefixPath, String[] values) {
        String path = prefixPath;
        if (values != null && values.length != 0) {
            path = ("/" + prefixPath + "/" + values[0]).replace("//", "/");
        }
        path = "/" + StrUtil.strip(path, "/", "/");
        return path;
    }

    private Set<String> getMethodsFromRequestMapping(RequestMapping requestMapping) {
        Set<String> methods = new HashSet<>();
        if (requestMapping.method() == null || requestMapping.method().length == 0) {
            methods = allMethods();
        } else {
            for (RequestMethod rm : requestMapping.method()) {
                if (rm == RequestMethod.GET) {
                    methods.add("GET");
                }
                if (rm == RequestMethod.PATCH) {
                    methods.add("PATCH");
                }
                if (rm == RequestMethod.PUT) {
                    methods.add("PUT");
                }
                if (rm == RequestMethod.DELETE) {
                    methods.add("DELETE");
                }
                if (rm == RequestMethod.POST) {
                    methods.add("POST");
                }
            }
        }
        return methods;
    }

    private Set<String> allMethods() {
        Set<String> allMethods = new HashSet<>();
        allMethods.add("GET");
        allMethods.add("PATCH");
        allMethods.add("PUT");
        allMethods.add("DELETE");
        allMethods.add("POST");
        return allMethods;
    }

    /**
     * 判断ctClass是否有@FeignClient注解
     * @param ctClass
     * @return
     */
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

    /**
     * 若ctClass有@RestController或@RequestMapping注解，则返回注解中value属性的值
     * 若ctClass无@RestController或@RequestMapping注解，则返回""
     * @param ctClass
     * @return
     */
    private String buildControllerPrefix(CtClass ctClass) {
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

    /**
     * 若ctClass有@FeignClient注解，则返回注解中name属性或value属性的值
     * 若ctClass无@FeignClient注解，则返回""
     * @param ctClass
     * @return
     */
    private String getFeignName(CtClass ctClass) {
        FeignClient feignClient = null;
        String feignName = "";
        try {

            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }
        if (feignClient == null) { // 若该类无FeignClient注解
            return "";
        }
        feignName = feignClient.value();
        if (feignName.equals("")) {
            feignName = feignClient.name();
        }
        return feignName;
    }

}
