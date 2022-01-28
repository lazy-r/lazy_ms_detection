package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/11/28
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiInfo {
    private String type;
    private String method;
    private String path;
}
