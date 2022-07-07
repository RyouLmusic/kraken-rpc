package org.kraken.core.remoting.net.netty.client;

import org.kraken.core.common.bean.URL;
import org.kraken.core.invoker.proxy.AbstractProxy;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.invoker.reference.ReferenceBean;

import java.lang.reflect.Constructor;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/9 11:10
 */
public class ConnectClientPool {
    private static volatile ConcurrentMap<String, AbstractClient> connectClientMap;        // (static) alread addStopCallBack
    private static volatile ConcurrentMap<String, Object> connectClientLockMap = new ConcurrentHashMap<>();

    /**
     * 一个请求service-client
     * @param url
     * @param connectClazz
     * @param referenceBean
     * @return
     * @throws Throwable
     */
    public static AbstractClient getPool(URL url, Class<? extends AbstractClient> connectClazz,
                                         final ReferenceBean referenceBean, AbstractProxy proxy) throws Throwable {
        String address = url.getAddress();
        // init base compont, avoid repeat init
        if (connectClientMap == null) {
            synchronized (AbstractClient.class) {
                if (connectClientMap == null) {
                    // 实例化
                    connectClientMap = new ConcurrentHashMap<>();
                    // 关闭需要进行的操作
                    referenceBean.getInvokerFactory().addStopCallBack(() -> {
                        if (connectClientMap.size() > 0) {
                            for (String key: connectClientMap.keySet()) {
                                AbstractClient client = connectClientMap.get(key);
                                try {
                                    client.close();
                                } catch (Throwable ignored) {
                                    // 如果另外一边先关闭的话，就会出现异常
                                }
                            }
                            connectClientMap.clear();
                        }
                    });
                }
            }
        }

        // get-valid client
        AbstractClient client = connectClientMap.get(address);
        if (client != null && client.isValidate()) {
            return client;
        }

        // 生成锁，各自地址有各自的锁，降低锁°
        Object clientLock = connectClientLockMap.get(address);
        if (clientLock == null) {
            connectClientLockMap.putIfAbsent(address, new Object());
            clientLock = connectClientLockMap.get(address);
        }

        // remove-create new client
        synchronized (clientLock) {

            // get-valid client, avlid repeat
            client = connectClientMap.get(address);
            if (client != null && client.isValidate()) {
                return client;
            }

            // remove old
            if (client != null) {
                client.close();
                connectClientMap.remove(address);
            }

            Constructor<? extends AbstractClient> clientConstructor = connectClazz.getConstructor(URL.class);
            AbstractClient client_0 = clientConstructor.newInstance(url);
            client_0.setCallable(proxy::getProviderAddress);
            // set pool
            try {
                // 启动，连接
                client_0.open();
                client_0.connect();
                connectClientMap.put(address, client_0);
            } catch (Throwable e) {
                client_0.close();
                throw e;
            }

            return client_0;
        }

    }
}
