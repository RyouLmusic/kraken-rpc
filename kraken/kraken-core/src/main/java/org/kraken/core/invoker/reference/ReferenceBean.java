package org.kraken.core.invoker.reference;

import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.fiter.Filter;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.invoker.InvokerFactory;
import org.kraken.core.invoker.config.DefaultConfig;
import org.kraken.core.invoker.config.MethodConfig;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.core.invoker.proxy.AbstractProxy;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.kraken.core.common.utils.ConfigUtils.makeServiceKey;

/**
 * rpc reference bean, use by api
 * 每个接口类都有 各自的Reference, (但是可以不需要的)
 * @author xuxueli 2015-10-29 20:18:32
 */
public class ReferenceBean {

    /*---------------------------client---------------------------------*/
    private Class<? extends AbstractClient> clientClass;
    private AbstractClient client;

    /*--------------------------- proxy --------------------------------*/
    private Class<? extends AbstractProxy> proxyClass;

    // ---------------------- registry interface ----------------------
    private Class<?> clazz;

    /*---------------------- config --------------------------*/
    private ReferenceConfig referenceConfig;
    private final Map<String, MethodConfig> methodConfigMap;
    private AppConfig appConfig = AppConfig.getAppConfig();

    private InvokeCallback<?> invokeCallback;

    public InvokeCallback<?> getInvokeCallback() {
        return invokeCallback;
    }

    public void setInvokeCallback(InvokeCallback<?> callback) {
        this.invokeCallback = callback;
    }
    public void setReferenceConfig(ReferenceConfig referenceConfig) {

        if (referenceConfig == null) return;
        if (referenceConfig.getClient() != null) {
            setClientClass(referenceConfig.getClient());
        }
        if (referenceConfig.getProxy() != null) {
            setProxyClass(referenceConfig.getProxy());
        }

        if (referenceConfig.getLoadBalanceType() != null) {
            appConfig.setLoadBalanceType(referenceConfig.getLoadBalanceType());
        }

        if (referenceConfig.getCompressType() != null) {
            appConfig.setCompressType(referenceConfig.getCompressType());
        }

        this.referenceConfig = referenceConfig;
    }

    public void putMethodConfig(MethodConfig config, String interfaceName, String methodName, Class<?>... clazz) {

        String key = makeServiceKey(interfaceName, methodName, clazz);
        methodConfigMap.put(key, config);
    }



    public ReferenceBean() {
        methodConfigMap = new ConcurrentHashMap<>();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                getInvokerFactory().stop();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }));
    }


    public <T> T getObject() throws Throwable {
        return (T) getObject(clazz);
    }

    /**
     * 实现远程调用
     * @return 返回代理之后的对象
     * @throws Throwable
     */
	private  <T> T getObject(Class<T> clazz) throws Throwable {
        if (Objects.isNull(clazz)) throw new AppException("没有注册接口", new NullPointerException());
        // 设置默认设置，外面也可以直接通过setProxyClass(),setClientClass()进行设置，或者setReferenceConfig(config)进行设置
        DefaultConfig defaultConfig = new DefaultConfig();
        if (proxyClass == null) {
            setProxyClass(defaultConfig.getProxy());
        }
        if (clientClass == null) {
            setClientClass(defaultConfig.getClient());
        }

        // init proxy
        Constructor<? extends AbstractProxy> proxyConstructor = proxyClass.getConstructor(Class.class, ReferenceBean.class);
        AbstractProxy proxy = proxyConstructor.newInstance(clientClass, this);

        proxy.setReferenceConfig(referenceConfig);
        proxy.setMethodConfigMap(methodConfigMap);
        return proxy.getInstance(clazz);
	}

    public void setClientClass(Class<? extends AbstractClient> clientClass) {
        this.clientClass = clientClass;
    }

    public void setProxyClass(Class<? extends AbstractProxy> proxyClass) {
        this.proxyClass = proxyClass;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }


    public void stop() throws Throwable {
        if (client == null) {
            System.exit(1);
        }
        client.close();
    }

    /**
     * 添加自定义的filter
     * @param filter 过滤
     */
    public void addFilter(Filter filter) {
        getInvokerFactory().addFilter(filter);
    }

    public InvokerFactory getInvokerFactory() {
        return InvokerFactory.getInstance();
    }

}
