package org.kraken.core.registry.zookeeper.core;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.RegisterException;
import org.kraken.core.registry.Subject;
import org.kraken.core.registry.zookeeper.bean.RegistryType;
import org.kraken.core.registry.zookeeper.bean.ZkManagementContext;
import org.kraken.core.registry.zookeeper.bean.ZkNode;
import org.kraken.core.registry.zookeeper.bean.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/22 13:20
 */
public class ZkServiceDiscover implements Subject {


    private List<ZkNode> discoverServiceAddress(String service, RegistryType type) {
        CuratorFramework zkClient = ZkManagementContext.getZkClient();
        switch (type) {
            case PROVIDER :
                return ZkUtils.getProviderChildrenNodes(zkClient, service);
            case CONSUMER :
                return ZkUtils.getConsumerChildrenNodes(zkClient, service);
            default: throw new RegisterException("discover service fail...");
        }

    }

    @Override
    public List<URL> subject(String service, RegistryType type) {
        List<ZkNode> zkNodes = discoverServiceAddress(service, type);

        return zkNodes
                .stream()
                .map((Function<ZkNode, URL>) zkNode -> zkNode)
                .collect(Collectors.toList());
    }

    /*@Override
    public List<String> updateSubject(String service, RegistryType type) {
        return null;
    }*/


    public void watcher(PathChildrenCacheListener function, String path) {
        CuratorFramework zkClient = ZkManagementContext.getZkClient();

        ZkUtils.registerWatcher(zkClient, function, path);
    }

}
