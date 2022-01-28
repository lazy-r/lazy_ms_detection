package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FZJApiInfo {
    private String className;
    private String funcName;
    private List<String> params;
    private String returnName;

    private String type;
    /* http请求方式 */
    private String method;
    /* http请求路径 */
    private String path;
}
