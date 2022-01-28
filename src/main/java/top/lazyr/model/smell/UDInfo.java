package top.lazyr.model.smell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/12/1
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UDInfo {
    private float udn;
    private float i;
}
