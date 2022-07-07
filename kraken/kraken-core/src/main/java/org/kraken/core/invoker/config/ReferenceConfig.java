package org.kraken.core.invoker.config;

import lombok.Data;
import org.kraken.core.compress.Compress;
import org.kraken.core.loadbalance.LoadBalance;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.invoker.proxy.AbstractProxy;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/11 23:23
 * TODO spring 通过注释来实现
 */
@Data

public class ReferenceConfig implements Config {

    /**
     * 设置接口的
     */
    private int active;

    /**
     * 这个接口在调用的时候，可以接受的timeout
     */
    private long timeout;
    /**
     * 请求的service版本
     */
    private String version;

    /**
     * client类型：Netty，NIO等
     */
    private Class<? extends AbstractClient> client;
    /**
     * 调用方式
     */
    private Class<? extends AbstractProxy> proxy;

    private LoadBalance.Type loadBalanceType;

    private Compress.Type compressType;
}
