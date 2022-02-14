package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 一个API对应一个Controller层中的一个有访问路径的方法
 * @author lazyr
 * @created 2021/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FZJApiInfo {
    /* API所在的类名 */
    private String className;
    /* API对应的方法名 */
    private String funcName;
    /* API方法的方法参数类路径 */
    private List<String> params;
    /* API方法的返回值类路径 */
    private String returnName;

    /* API的方式：HTTP or RPC */
    private String type;
    /* http请求方式, 一个API的请求方式可能会有多种 */
    private Set<String> methods;
    /* http请求路径 */
    private String path;
}
