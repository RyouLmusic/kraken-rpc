package org.kraken.spring.invoker;

import lombok.extern.slf4j.Slf4j;
import org.kraken.core.common.exception.InvokerException;
import org.kraken.core.fiter.Filter;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.invoker.config.MethodConfig;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.core.invoker.proxy.CallbackProxy;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.spring.annotation.KrakenFilter;
import org.kraken.spring.annotation.KrakenReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 13:27
 */
@Slf4j
public class InvokerFactory implements InitializingBean, DisposableBean, BeanFactoryAware, InstantiationAwareBeanPostProcessor {

    final List<Filter> filters = new ArrayList<>();

    private org.kraken.core.invoker.InvokerFactory invoker;

    private ReferenceConfig referenceConfig;

    public void setReferenceConfig(ReferenceConfig referenceConfig) {
        this.referenceConfig = referenceConfig;
    }

    // TODO 可以细化到方法的配置
    private MethodConfig methodConfig;

    private ConfigurableListableBeanFactory beanFactory;



    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {

        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {

            KrakenReference reference = field.getAnnotation(KrakenReference.class);
            if (reference != null) {
                // valid
                Class<?> interfaceName = field.getType();
                if (!interfaceName.isInterface()) {
                    throw new InvokerException("[Kraken], Reference must be interface.");
                }

                // 构造各自的reference类
                ReferenceBean referenceBean = new ReferenceBean();
//                System.out.println("--------------------------------------");
//                System.out.println("referenceBean  :" + referenceBean);
                // 添加过滤器

                if (referenceConfig == null) {
                    referenceConfig = new ReferenceConfig();
                }
                // TODO 其他配置可以继续加
                // 覆盖掉之前总的配置，每个interface都有自己的配置
                referenceConfig.setActive(reference.active());
                referenceConfig.setTimeout(reference.timeout());
                referenceConfig.setVersion(reference.version());

                referenceBean.setReferenceConfig(referenceConfig);

                // 添加filter
                filters.forEach(referenceBean::addFilter);

                // 注册服务
                referenceBean.setClazz(field.getType());
                Object service = null;
                try {
                    service = referenceBean.getObject();

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, service);



                    if (referenceConfig.getProxy() != null && referenceConfig.getProxy() == CallbackProxy.class) {
                        InvokeCallback<?> callback = null;

                        System.out.println(bean.getClass().getTypeName());
                        try {
                            // 对 bean内部类 实现callback类的支持
                            if (reference.callback().contains("$")) {
                                Class<?> callbackClass = Class.forName(reference.callback());
                                Constructor<?> constructor = callbackClass.getDeclaredConstructors()[0];
                                constructor.setAccessible(true);
                                if (constructor.getParameterTypes().length > 0) {
                                    callback  = (InvokeCallback<?>)constructor.newInstance(bean);
                                } else {
                                    callback  = (InvokeCallback<?>)constructor.newInstance();
                                }

                            } else {
                                // 非内部类
                                Class<?> callbackClass = Class.forName(reference.callback());
                                Constructor<?> constructor = callbackClass.getConstructor();
                                constructor.setAccessible(true);
                                callback = (InvokeCallback<?>) constructor.newInstance();

                            }
                            referenceBean.setInvokeCallback(callback);
                        } catch (ClassNotFoundException e) {
                            throw new InvokerException("callback class not found");
                        }
                    }
                } catch (Throwable e) {

                    throw new InvokerException("Spring invoker err", e);
                }
                log.info("[Kraken], invoker factory init reference bean success. service name = {},", beanName);

            }
        }


        return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void destroy() throws Exception {
        invoker.stop();
    }

    /**
     * 初始化invoker
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        invoker = org.kraken.core.invoker.InvokerFactory.getInstance();
    }


    public void addFilter(Filter filter) {
        filters.add(filter);
    }
}
