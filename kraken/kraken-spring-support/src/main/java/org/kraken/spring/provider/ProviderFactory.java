package org.kraken.spring.provider;

import org.kraken.core.common.exception.AppException;
import org.kraken.core.invoker.provider.ProvideServiceConfig;
import org.kraken.spring.annotation.KrakenService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 13:27
 */
public class ProviderFactory extends org.kraken.core.invoker.provider.ProviderFactory implements ApplicationContextAware, InitializingBean, DisposableBean {

    @Override
    public void destroy() throws Exception {
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(KrakenService.class);

        if (serviceBeanMap.size() > 0) {
            for (Object bean : serviceBeanMap.values()) {
                // valid
                if (bean.getClass().getInterfaces().length ==0) {
                    throw new AppException("[kraken], service must inherit interface.");
                }
                // add service
                KrakenService service = bean.getClass().getAnnotation(KrakenService.class);

                String interfaceName = bean.getClass().getInterfaces()[0].getName();
                String version = service.version();

                String group = service.group();
                int weight = service.weight();
                int warmup = service.warmup();
                ProvideServiceConfig config = ProvideServiceConfig.builder()
                        .interfaceName(interfaceName)
                        .version(version)
                        .group(group)
                        .weight(weight)
                        .warmup(warmup)
                        .build();
                super.addService(config, bean);
            }
        }
    }
}
