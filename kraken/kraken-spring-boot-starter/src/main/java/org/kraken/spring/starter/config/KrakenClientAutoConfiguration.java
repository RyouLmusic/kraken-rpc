package org.kraken.spring.starter.config;

import org.kraken.core.fiter.Filter;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.spring.annotation.KrakenFilter;
import org.kraken.spring.invoker.InvokerFactory;
import org.kraken.spring.starter.properties.KrakenClientProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 22:05
 */
@Configuration
@Import({KrakenClientProperties.class})
public class KrakenClientAutoConfiguration implements InstantiationAwareBeanPostProcessor {


    @Autowired
    private ApplicationContext applicationContext;
    @Bean
    @ConditionalOnMissingBean(InvokerFactory.class)
    public InvokerFactory invokerFactory(KrakenClientProperties properties) {
        InvokerFactory invokerFactory = new InvokerFactory();

        ReferenceConfig referenceConfig = new ReferenceConfig();

        BeanUtils.copyProperties(properties, referenceConfig);
        invokerFactory.setReferenceConfig(referenceConfig);

        Map<String,Object> objectMap = applicationContext.getBeansWithAnnotation(KrakenFilter.class);
        objectMap.forEach((name, bean) -> {
            if (bean instanceof Filter) {
                invokerFactory.addFilter((Filter) bean);
            }
        });

        return invokerFactory;
    }

}
