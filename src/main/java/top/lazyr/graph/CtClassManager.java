package top.lazyr.graph;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author lazyr
 * @created 2021/11/20
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
            logger.error("OUTER CLASS NOT FOUND => " + classAbsolutePath + ", err: " + e.getMessage());
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
            logger.error("inner class not found => " + className + ", err: " + e.getMessage());
        }
        return ctClass;
    }


}
