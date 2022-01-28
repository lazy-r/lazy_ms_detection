package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.lazyr.graph.Graph;

import java.util.List;

/**
 * @author lazyr
 * @created 2021/11/22
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class SvcInfo {
    private String svcName;
    private List<ApiInfo> apiNames;
    private List<CallInfo> calls;
    /** Func粒度的Node */
    private Graph funcGraph;
}
