package top.lazyr.model.smell;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/12/3
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HLInfo {
    private Float hwo;
    private Float hwi;

}
