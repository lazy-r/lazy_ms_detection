package top.lazyr.microservice;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.model.Api;
import top.lazyr.model.Feign;
import top.lazyr.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lazyr
 * @created 2022/4/22
 */
public class SvcParser {
    private static Logger logger = LoggerFactory.getLogger(SvcParser.class);
    private CtClassManager ctClassManager;
    private Map<String, Feign> feignFileName2Feign;
    private Service service;

    public SvcParser(Map<String, Feign> feignFileName2Feign) {
        this.feignFileName2Feign = feignFileName2Feign;
        ctClassManager = CtClassManager.getCtClassManager();
    }

    public Service parse(String svcPath) {
        List<CtClass> ctClasses = ctClassManager.extractCtClass(svcPath);
        String svcName = StringUtil.getCurrentCatalog(svcPath);
        InternalGraphParser graphParser = new InternalGraphParser(feignFileName2Feign.keySet());
        InternalGraph graph = graphParser.parse(svcPath);
        this.service = new Service(svcName, graph);




        return null;
    }




}
