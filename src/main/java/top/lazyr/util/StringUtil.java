package top.lazyr.util;

/**
 * @author lazyr
 * @created 2021/11/26
 */
public class StringUtil {
    public static String getCurrentCatalog(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
