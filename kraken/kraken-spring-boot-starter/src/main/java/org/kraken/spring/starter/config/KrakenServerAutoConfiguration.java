package org.kraken.spring.starter.config;

import org.kraken.core.invoker.provider.ServerConfig;
import org.kraken.core.remoting.net.netty.server.NettyServer;
import org.kraken.spring.provider.ProviderFactory;
import org.kraken.spring.starter.properties.KrakenServerProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/29 11:13
 */
@Configuration
@Import({KrakenServerProperties.class})
public class KrakenServerAutoConfiguration {

    @Bean("krakenProvider")
    @ConditionalOnMissingBean(ProviderFactory.class)
    public ProviderFactory providerFactory(KrakenServerProperties properties) {
        ProviderFactory providerFactory = new ProviderFactory();

        ServerConfig serverConfig = new ServerConfig();
        providerFactory.setServerClass(NettyServer.class);

        BeanUtils.copyProperties(properties, serverConfig);
        providerFactory.setServerConfig(serverConfig);

        return providerFactory;
    }
}
