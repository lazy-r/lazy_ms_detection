package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FZJCallInfo {
    private String targetServiceName;
    private String targetAPIMethod;
    private String targetAPIPath;

    private FZJApiInfo srcApiInfo;
    private FZJApiInfo targetApiInfo;
    private int weight;
}
