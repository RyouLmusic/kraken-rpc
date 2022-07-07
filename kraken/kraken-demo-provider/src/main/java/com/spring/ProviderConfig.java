package com.spring;

import org.kraken.core.invoker.provider.ServerConfig;
import org.kraken.core.remoting.net.netty.server.NettyServer;
import org.kraken.spring.provider.ProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/22 23:59
 */
@Configuration
@ComponentScan(basePackages = {"com"})
public class ProviderConfig {

    @Bean
    public ProviderFactory springProviderFactory() {

        ProviderFactory providerFactory = new ProviderFactory();
        providerFactory.setServerClass(NettyServer.class);
        ServerConfig serverConfig = new ServerConfig();

        serverConfig.setPort(8003);

        providerFactory.setServerConfig(serverConfig);
        return providerFactory;
    }
}
