package com.hbk.spring;

import com.hbk.spring.filter.AFilter;
import com.hbk.spring.filter.BFilter;
import org.kraken.core.compress.Compress;
import org.kraken.core.fiter.Filter;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.core.invoker.proxy.CallbackProxy;
import org.kraken.spring.invoker.InvokerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 14:41
 */
@Configuration
@ComponentScan(basePackages = {"com.hbk"})
public class InvokerConfig {

    @Autowired
    private AFilter filter;

    @Autowired
    private BFilter bFilter;
    @Bean
    public InvokerFactory invokerExecutor() {
        InvokerFactory invokerFactory = new InvokerFactory();
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setProxy(CallbackProxy.class);
        referenceConfig.setCompressType(Compress.Type.Gzip);
        referenceConfig.setVersion("1.0");
        invokerFactory.setReferenceConfig(referenceConfig);

        invokerFactory.addFilter(filter);
        invokerFactory.addFilter(bFilter);
        return invokerFactory;
    }
}
