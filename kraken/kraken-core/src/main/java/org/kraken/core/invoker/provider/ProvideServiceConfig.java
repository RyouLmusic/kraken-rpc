package org.kraken.core.invoker.provider;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 21:42
 */
@Data
@Builder
public class ProvideServiceConfig {

    String interfaceName;
    String version;
    String group;
    int weight;
    int warmup;

}
