package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.lazyr.graph.Graph;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/12/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FZJSvcInfo {
    private String svcName;
    private List<FZJApiInfo> apiNames;
    private List<FZJCallInfo> calls;
    /** Func粒度的Node */
    private Graph funcGraph;
}
