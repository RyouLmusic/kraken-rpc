package org.kraken.core.registry;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.kraken.core.common.bean.URL;
import org.kraken.core.registry.zookeeper.bean.RegistryType;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/3 20:34
 * 订阅服务(发现服务)
 */
public interface Subject {

    /**
     * 获取rpc注册中心服务名称
     *
     * @param service 服务名称
     * @param type {@link RegistryType}
     * @return 注册中心地址 String : < host:port >
     */
    List<URL> subject(String service, RegistryType type);
//    List<String> updateSubject(String service, RegistryType type);

    void watcher(PathChildrenCacheListener function, String path);
}
