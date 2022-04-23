package top.lazyr.microservice;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.lazyr.util.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyr
 * @created 2022/1/25
 */
public class CtClassManager {
    private static Logger logger = LoggerFactory.getLogger(CtClassManager.class);
    private static CtClassManager manager;
    // TODO: 代优化单例模式
    private static ClassPool classPool = new ClassPool(true);

    public static CtClassManager getCtClassManager() {
        if (manager == null) {
            manager.ensureLogManagerInitialized();
        }
        return manager;
    }

    private CtClassManager() {
    }

    private static final void ensureLogManagerInitialized() {
        // TODO: 单例模式
        manager = new CtClassManager();
    }


    /**
     * 获取指定路径的CtClass
     * @param classAbsolutePath
     * @return
     */
    public CtClass getOuterCtClass(String classAbsolutePath) {
        CtClass ctClass = null;
        try {
            ctClass = classPool.makeClass(new FileInputStream(classAbsolutePath));
        } catch (IOException e) {
            logger.error("the path ({}) of non system class not found , err: {}", classAbsolutePath, e.getMessage());
        }
        return ctClass;
    }

    /**
     * 获取已加载的CtClass
     * @param className
     * @return
     */
    public CtClass getCtClass(String className) {
        CtClass ctClass = null;
        try {
//            classPool.insertClassPath(new ClassClassPath(Class.forName(className)));//为防止项目被打成jar包，请使用该语句
            ctClass = classPool.getCtClass(className);
        } /*catch (ClassNotFoundException e) {
            logger.error("inner class file not found => " + className + ", err: " + e.getMessage());
        }*/ catch (NotFoundException e) {
            logger.error("system class not found => " + className + ", err: " + e.getMessage());
        }
        return ctClass;
    }


    /**
     * 从源码文件提取CtClass对象
     * @param sourceCodePath
     * @return
     */
    public List<CtClass> extractCtClass(String sourceCodePath) {
        List<String> filesAbsolutePath = FileUtil.getFilesAbsolutePath(sourceCodePath, ".class");
        List<CtClass> ctClasses = new ArrayList<>();
        for (String fileAbsolutePath : filesAbsolutePath) {
            CtClass ctClass = getOuterCtClass(fileAbsolutePath);
            ctClasses.add(ctClass);
        }
        return ctClasses;
    }

}
