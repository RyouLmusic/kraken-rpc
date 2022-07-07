package org.kraken.core.invoker.config;


import lombok.Data;
import org.kraken.core.invoker.proxy.SyncProxy;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.net.netty.client.NettyClient;
import org.kraken.core.invoker.proxy.AbstractProxy;

import static org.kraken.core.common.bean.Constants.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/13 0:14
 */
@Data
public class DefaultConfig implements Config {
    /**
     * 设置接口的
     */
    private int active = ACTIVE;

    /**
     * 这个接口在调用的时候，可以接受的timeout
     */
    private long timeout = TIMEOUT;
    /**
     * 请求的service版本
     */
    private String version = VERSION;


    /**
     * client类型：Netty，NIO等
     */
    private Class<? extends AbstractClient> client = NettyClient.class;
    /**
     * 调用方式
     */
    private Class<? extends AbstractProxy> proxy = SyncProxy.class;
}
