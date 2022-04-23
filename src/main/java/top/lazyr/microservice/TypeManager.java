package top.lazyr.microservice;

import cn.hutool.core.util.StrUtil;
import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.lazyr.model.Api;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lazyr
 * @created 2022/4/22
 */
public class TypeManager {
    private static Logger logger = LoggerFactory.getLogger(TypeManager.class);

    /**
     * 判断ctClass是否有@FeignClient注解
     * @param ctClass
     * @return
     */
    public static boolean isFeign(CtClass ctClass) {
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

    public static boolean isAPI(CtClass ctClass) {
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


    /**
     * 若arrName为数组，则返回数组的类型类名
     * - 若数组的类型为基本数据类型则将其转换为包装类再返回
     * - 若数组的类型为普通类则返回普通类
     * 若arrName为普通类，则不做处理直接返回arrName
     * @param arrName
     * @return
     */
    public static String arr2Class(String arrName) {
        if (!arrName.contains("[]")) {
            return arrName;
        }
        return unboxing(arrName.substring(0, arrName.indexOf("[")));
    }

    /**
     * 若baseType为基本数据类型，则将其转换为包装器类型
     * 若baseType不为基本数据类型，则不做处理
     * @param baseType
     * @return
     */
    public static String unboxing(String baseType) {
        String className = baseType;
        switch (baseType) {
            case "byte":
                className = Byte.class.getName();
                break;
            case "short":
                className = Short.class.getName();
                break;
            case "int":
                className = Integer.class.getName();
                break;
            case "long":
                className = Long.class.getName();
                break;
            case "float":
                className = Float.class.getName();
                break;
            case "double":
                className = Double.class.getName();
                break;
            case "char":
                className = Character.class.getName();
                break;
            case "boolean":
                className = Boolean.class.getName();
                break;
        }
        return className;
    }


    /**
     * 若className包含$，则返回所在的文件名
     * 若className不包含$，则不做处理直接返回className
     * @param className
     * @return
     */
    public static String extractFileName(String className) {
        if (!className.contains("$")) {
            return className;
        }
        return className.substring(0, className.indexOf("$"));
    }


    /**
     * 若ctClass有@FeignClient注解，则返回注解中name属性或value属性的值
     * 若ctClass无@FeignClient注解，则返回""
     * @param ctClass
     * @return
     */
    public static String extractSvcName(CtClass ctClass) {
        FeignClient feignClient = null;
        String svcName = "";
        try {
            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }
        if (feignClient == null) { // 若该类无FeignClient注解
            return "";
        }
        svcName = feignClient.value();
        if (svcName.equals("")) {
            svcName = feignClient.name();
        }
        return svcName;
    }

    /**
     * 返回结果是以 "/" 开头
     * @param ctClass
     * @return
     */
    public static String extractFeignPrefix(CtClass ctClass) {
        FeignClient feignClient = null;
        String prefix = "";
        try {
            feignClient = (FeignClient) ctClass.getAnnotation(FeignClient.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class feign annotation failed: " + e.getMessage());
        }
        if (feignClient == null) { // 若该类无FeignClient注解
            return "/";
        }
        prefix = "/" + feignClient.path();
        return uniqueBackslash(prefix);  // 将 // 替换为 /
    }

    /**
     * 将url中所有的 // 替换为 /
     * @param url
     * @return
     */
    public static String uniqueBackslash(String url) {
        return url.replaceAll("//", "/");
    }

    /**
     * a.b.c.func(java.lang.String) -> func(java.lang.String)
     * @param longMethodName
     * @return
     */
    public static String extractMethodName(String longMethodName) {
        String[] info = longMethodName.split("\\(");
        int lastIndex = info[0].lastIndexOf(".");
        String methodName = info[0].substring(lastIndex + 1);
        return methodName + "(" + info[1];
    }

    /**
     * 判断ctMethod是否为接口方法
     * @param ctMethod
     * @return
     */
    public static boolean isApiFunc(CtMethod ctMethod) {
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
        return requestMapping != null ||
                getMapping != null ||
                postMapping != null ||
                putMapping != null ||
                deleteMapping != null ||
                patchMapping != null;
    }

    public static Set<Api> extractApiFromApiFunc(CtMethod ctMethod, String feignPrefix) {
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

        String url = "";
        Set<String> methods = new HashSet<>();
        if (requestMapping != null) {
            methods = getMethodsFromRequestMapping(requestMapping);
            url = buildUrl(feignPrefix, requestMapping.value());
        }
        if (getMapping != null) {
            methods.add(Api.GET);
            url = buildUrl(feignPrefix, getMapping.value());
        }
        if (postMapping != null) {
            methods.add(Api.POST);
            url = buildUrl(feignPrefix, postMapping.value());
        }
        if (putMapping != null) {
            methods.add(Api.PUT);
            url = buildUrl(feignPrefix, putMapping.value());
        }
        if (deleteMapping != null) {
            methods.add(Api.DELETE);
            url = buildUrl(feignPrefix, deleteMapping.value());
        }
        if (patchMapping != null) {
            methods.add(Api.PATCH);
            url = buildUrl(feignPrefix, patchMapping.value());
        }

        Set<Api> apis = new HashSet<>();
        for (String method : methods) {
            Api api = Api.builder().url(uniqueBackslash(url)).method(method).build();
            apis.add(api);
        }

        return apis;
    }

    private static Set<String> getMethodsFromRequestMapping(RequestMapping requestMapping) {
        Set<String> methods = new HashSet<>();
        if (requestMapping.method() == null || requestMapping.method().length == 0) {
            methods = allMethods();
        } else {
            for (RequestMethod rm : requestMapping.method()) {
                if (rm == RequestMethod.GET) {
                    methods.add(Api.GET);
                }
                if (rm == RequestMethod.PATCH) {
                    methods.add(Api.PATCH);
                }
                if (rm == RequestMethod.PUT) {
                    methods.add(Api.PUT);
                }
                if (rm == RequestMethod.DELETE) {
                    methods.add(Api.DELETE);
                }
                if (rm == RequestMethod.POST) {
                    methods.add(Api.POST);
                }
            }
        }
        return methods;
    }

    private static Set<String> allMethods() {
        Set<String> allMethods = new HashSet<>();
        allMethods.add("GET");
        allMethods.add("PATCH");
        allMethods.add("PUT");
        allMethods.add("DELETE");
        allMethods.add("POST");
        return allMethods;
    }

    /**
     * prefix = a, values[0] = b, 返回 /a/b
     * prefix = a, values[0] = b/, 返回 /a/b
     * prefix = /a, values[0] = /b, 返回 /a/b
     * prefix = , values[0] = b, 返回 /b
     * prefix = , values[0] = , 返回 /
     * @param prefix
     * @param values
     * @return
     */
    private static String buildUrl(String prefix, String[] values) {
        String url = prefix;
        if (values != null && values.length != 0) {
            url = ("/" + prefix + "/" + values[0]).replace("//", "/");
        }
        url = "/" + StrUtil.strip(url, "/", "/");
        return url;
    }

    /**
     * 若ctClass有@RestController或@RequestMapping注解，则返回注解中value属性的值
     * 若ctClass无@RestController或@RequestMapping注解，则返回"/"
     * @param ctClass
     * @return
     */
    public static String extractApiPrefix(CtClass ctClass) {
        RestController restController = null;
        RequestMapping requestMapping = null;
        String prefix = "";
        try {
            restController = (RestController)ctClass.getAnnotation(RestController.class);
            requestMapping = (RequestMapping)ctClass.getAnnotation(RequestMapping.class);
        } catch (ClassNotFoundException e) {
            logger.error("pares class api annotation failed: " + e.getMessage());
        }
        if (restController != null) {
            prefix = restController.value();
        }

        if (requestMapping != null) {
            String[] prefixValues = requestMapping.value();
            if (prefixValues.length != 0) {
                prefix = prefixValues[0];
            }
            String[] prefixPaths = requestMapping.path();
            if (prefixPaths.length != 0) {
                prefix = prefixPaths[0];
            }
        }
        return uniqueBackslash("/" + prefix);
    }
}
