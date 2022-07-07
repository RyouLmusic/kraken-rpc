package org.kraken.core.registry;

import org.kraken.core.common.bean.URL;
import org.kraken.core.registry.zookeeper.bean.RegistryType;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/3 20:33
 * 注册服务(发布服务)
 *
 * 在此接口实现SPI机制
 */
public interface Publisher {
    /**
     * 进行注册服务操作
     * @param service service name：com.kraken.service.HelloService
     * @param url host:port, 其他信息
     * @param type provider or consumer
     */
    void register(String service, URL url, RegistryType type);

    /**
     * 进行注销服务操作
     * @param service 服务名称：com.kraken.service.HelloService
     * @param url: host:port
     * @param type: provider or consumer
     */
    void unRegister(String service, URL url, RegistryType type);
}
