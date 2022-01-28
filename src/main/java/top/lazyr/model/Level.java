package top.lazyr.model;

/**
 * @author lazyr
 * @created 2021/11/20
 */
public class Level {
    private final String name;
    private final int value;

    public static final int SERVICE_LEVEL = 4;
    public static final int PACKAGE_LEVEL = 3;
    public static final int CLASS_LEVEL = 2;
    public static final int FUNC_LEVEL = 1;

    public static final Level SERVICE = new Level("SERVICE", SERVICE_LEVEL);
    public static final Level PACKAGE = new Level("PACKAGE", PACKAGE_LEVEL);
    public static final Level CLASS = new Level("CLASS", CLASS_LEVEL);
    public static final Level FUNC = new Level("FUNC", FUNC_LEVEL);

    protected Level(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public final int intValue() {
        return value;
    }

    public final String stringValue() {
        return name;
    }

    @Override
    public String toString() {
        return "Level: " + name;
    }
}
