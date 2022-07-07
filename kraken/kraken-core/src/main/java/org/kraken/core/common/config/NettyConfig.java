package org.kraken.core.common.config;

import org.kraken.core.common.annotation.PropertiesValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/12 11:25
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class NettyConfig {

    @PropertiesValue(property = "h_rpc.netty.sendBufSize", defaultValue = "2048")
    private int sendBufSize;
    @PropertiesValue(property = "h_rpc.netty.rcvBufSize", defaultValue = "2048")
    private int rcvBufSize;

    @PropertiesValue(property = "h_rpc.netty.connectTimeoutMillis", defaultValue = "2000")
    private int connectTimeoutMillis;

    /**
     * 心跳间隔时间
     */
    @PropertiesValue(property = "kraken.netty.heartbeatIntervalTime", defaultValue = "4")
    private int heartbeatIntervalTime;

}
