package top.lazyr.constant;

/**
 * @author lazyr
 * @created 2022/2/20
 */
public interface Printer {
    String SEPARATOR = "=========================";
    static void printTitle(Object title) {
        System.out.println(SEPARATOR + title + SEPARATOR);
    }
}
