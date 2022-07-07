package com.common;

import com.hbk.service.HelloService;
import com.hbk.service.LoginService;

import org.kraken.core.compress.Compress;
import org.kraken.core.invoker.provider.ProvideServiceConfig;
import org.kraken.core.invoker.provider.ProviderFactory;
import org.kraken.core.remoting.net.netty.server.NettyServer;
import com.service.HelloServiceImpl;
import com.service.LoginServiceImpl;

import java.net.UnknownHostException;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/24 23:05
 */
public class Provider {


    public static void main(String[] args) throws UnknownHostException {
        ProviderFactory providerFactory = new ProviderFactory();

        providerFactory.setPort(8002);
        providerFactory.setServerClass(NettyServer.class);
        ProvideServiceConfig config = ProvideServiceConfig.builder().build();

        config.setInterfaceName(HelloService.class.getName());
        config.setVersion("1.0");
        config.setGroup("");
        config.setWeight(5);
        config.setWarmup(0);
        providerFactory.setCompressType(Compress.Type.Gzip);
        providerFactory.addService(config, new HelloServiceImpl());
        providerFactory.addService(LoginService.class.getName(), "", "", 0, 0, new LoginServiceImpl());
        providerFactory.start();
    }
}