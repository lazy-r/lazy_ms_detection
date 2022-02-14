package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author lazyr
 * @created 2021/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FZJCallInfo {
    /* 目标微服务名 */
    private String targetServiceName;
    /* 目标HTTP请求方式 */
    private Set<String> targetAPIMethods;
    /* 目标HTTP请求路径 */
    private String targetAPIPath;

    /* 入节点API信息 */
    private FZJApiInfo srcApiInfo;
    /* 出节点API信息 */
    private FZJApiInfo targetApiInfo;
    /* 调用权重（静态代码中调用次数） */
    private int weight;
}
