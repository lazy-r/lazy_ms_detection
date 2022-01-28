package top.lazyr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lazyr
 * @created 2021/11/22
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class CallInfo {
    private String targetServiceName;
    private String targetAPIMethod;
    private String targetAPIPath;
    private int weight;

    @Override
    public String toString() {
        return "CallInfo{" +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", targetAPIName='" + targetAPIPath + '\'' +
                ", weight=" + weight +
                '}';
    }
}
