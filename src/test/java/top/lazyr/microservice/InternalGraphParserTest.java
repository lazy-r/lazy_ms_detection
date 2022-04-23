package top.lazyr.microservice;

import javassist.CtClass;
import org.junit.Test;
import top.lazyr.util.FileUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class InternalGraphParserTest {

    @Test
    public void parse() {

        String msPath = "/Users/lazyr/Work/projects/devops/test/data/dop";
        CtClassManager ctClassManager = CtClassManager.getCtClassManager();
        List<CtClass> ctClasses = ctClassManager.extractCtClass(msPath);
        Set<String> feignFileNames = extractFeignFileNames(ctClasses);
        String svcPath = "/Users/lazyr/Work/projects/devops/test/data/dop/application-server";
        InternalGraphParser internalGraphParser = new InternalGraphParser(feignFileNames);
        InternalGraph internalGraph = internalGraphParser.parse(svcPath);
        GraphWriter.printInfo(internalGraph);
    }


    private Set<String> extractFeignFileNames(List<CtClass> ctClasses) {
        Set<String> feignFileNames = new HashSet<>();
        for (CtClass ctClass : ctClasses) {
            if (TypeManager.isFeign(ctClass)) {
                feignFileNames.add(ctClass.getName());
            }
        }
        return feignFileNames;
    }
}
