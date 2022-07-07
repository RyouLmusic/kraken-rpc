package org.kraken.core.invoker.provider;

import lombok.Data;
import org.kraken.core.compress.Compress;
import org.kraken.core.loadbalance.LoadBalance;
import org.kraken.core.remoting.net.AbstractServer;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/26 9:48
 */
@Data
public class ServerConfig {

    private String serializerType;

    private String registryAddress;

    private String zkRegisterRootPath;

    private int port;

    private Class<? extends AbstractServer> serverClass;

    private int zkMaxWaitTime;

    private int heartbeatIntervalTime;

    private LoadBalance.Type loadBalanceType;

    private Compress.Type compressType;
}
