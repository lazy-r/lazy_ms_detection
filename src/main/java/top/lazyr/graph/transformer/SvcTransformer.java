package top.lazyr.graph.transformer;

import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.lazyr.graph.Graph;
import top.lazyr.graph.filter.Filter;
import top.lazyr.graph.filter.InnerClassFilter;
import top.lazyr.graph.filter.ProjectFilter;
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
 * @created 2021/12/1
 */
public class SvcTransformer extends Transformer{
    private static Logger logger = LoggerFactory.getLogger(SvcTransformer.class);
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
        List<Node> funcNodes = funcGraph.getNodes();
        this.nodesMap = nodes2Map(funcNodes);
        Set<CtClass> set = new HashSet<>();
        for (Node funcNode : funcNodes) {
            set.add((CtClass) funcNode.getNodeInfo());
        }

        SvcInfo svcInfo = new SvcInfo();
        List<ApiInfo> apis = new ArrayList<>();
        List<CallInfo> calls = new ArrayList<>();
        for (CtClass ctClass : set) {
            List<ApiInfo> apiInfos = buildSvcAPI(ctClass);
            apis.addAll(apiInfos);
            List<CallInfo> callInfos = buildSvcCall(ctClass);
            calls.addAll(callInfos);
        }
        svcInfo.setSvcName(svcName);
        svcInfo.setFuncGraph(funcGraph);
        svcInfo.setCalls(calls);
        svcInfo.setApiNames(apis);
        Node<SvcInfo> svcNode = new Node<>();
        svcNode.setNodeInfo(svcInfo);
        svcNode.setFrom("PROJECT");
        svcNode.setName(svcName);
        svcNode.setLevel(Level.SERVICE);
        return svcNode;
    }

    protected Map<String, Node> nodes2Map(List<Node> classNodes) {
        Map<String, Node> nodesMap = new HashMap<>();
        for (Node classNode : classNodes) {
            nodesMap.put(classNode.getName(), classNode);
        }
        return nodesMap;
    }

    private List<ApiInfo> buildSvcAPI(CtClass ctClass) {
        boolean isAPI = isAPI(ctClass);
        String prefixPath = "";
        List<ApiInfo> apis = new ArrayList<>();
        if (isAPI) {
            prefixPath = buildPrefixPath(ctClass);
            apis.addAll(getFuncAPI(ctClass, prefixPath));
        }
        return apis;

    }

    private List<CallInfo> buildSvcCall(CtClass ctClass) {
        boolean isFeign = isFeign(ctClass);
        List<CallInfo> callInfos = new ArrayList<>();
        String feignName = "";
        if (isFeign) {
            feignName = buildFeignName(ctClass);
            callInfos.addAll(getFeign(feignName, ctClass));
        }
        return callInfos;
    }

    private List<CallInfo> getFeign(String feignName, CtClass ctClass) {
        CtMethod[] methods = ctClass.getMethods();
        List<CallInfo> callInfos = new ArrayList<>();

        for (CtMethod m : methods) {
            CallInfo callInfo = new CallInfo();
            Node funcNode = nodesMap.get(m.getLongName());
            if (funcNode == null) {
                continue;
            }
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
                patchMapping = (PatchMapping)m.getAnnotation(PatchMapping.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String path = "";
            String method = "";
            int weight = funcNode.getAfferentNum();
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

            if (path.equals("")) {
                continue;
            }

            callInfo.setTargetAPIPath(path);
            callInfo.setTargetAPIMethod(method);
            callInfo.setWeight(weight);
            callInfo.setTargetServiceName(feignName);

            callInfos.add(callInfo);
        }
        return callInfos;
    }

    private List<ApiInfo> getFuncAPI(CtClass ctClass, String prefixPath) {
        CtMethod[] methods = ctClass.getMethods();
        List<ApiInfo> apiInfos = new ArrayList<>();
        for (CtMethod m : methods) {
            ApiInfo apiInfo = new ApiInfo();
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
            apiInfos.add(apiInfo);
        }
        return apiInfos;
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
            System.out.println(prefixPath);
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
}
