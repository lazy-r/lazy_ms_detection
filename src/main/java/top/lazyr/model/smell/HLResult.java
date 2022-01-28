package top.lazyr.model.smell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/12/3
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class HLResult {
    private Map<String, HLInfo> smellInfos;
    private List<String> smellNodes;
}
