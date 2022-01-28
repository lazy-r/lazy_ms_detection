package top.lazyr.model.smell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author lazyr
 * @created 2021/12/1
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UDResult {
    private Map<String, UDInfo> smellInfos;
    private Map<String, List<String>> smellNodes;
}
