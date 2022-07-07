package org.kraken.core.registry.zookeeper.core;

import org.kraken.core.common.bean.URL;
import org.kraken.core.registry.Publisher;
import org.kraken.core.registry.ZkService;
import org.kraken.core.registry.zookeeper.bean.RegistryType;
import org.kraken.core.registry.zookeeper.bean.ZkManagementContext;
import org.kraken.core.registry.zookeeper.bean.ZkNode;
import org.kraken.core.registry.zookeeper.bean.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 16:18
 *
 * zk服务发布(服务端将可以发布的接口信息发布到zk)
 * TODO 添加SPI机制
 */
public class ZkServicePublisher extends ZkService implements Publisher {

    /**
     * 发布服务(就是往zk服务器中添加一个持久节点)
     * @param node 通过node获取 需要创建的节点的path
     */
    private void publishService(ZkNode node) {
        CuratorFramework zkClient = ZkManagementContext.getZkClient();
        ZkUtils.createPersistentProviderNode(zkClient,node);
    }

    private void clearService(ZkNode node) {
        CuratorFramework zkClient = ZkManagementContext.getZkClient();
        ZkUtils.clearRegistryByAddress(zkClient, node);
    }


    @Override
    public void register(String service, URL url, RegistryType type) {
        ZkNode node = ZkNode.UrlToZkNode(url);
        node.setRegistryType(type);
        node.setServiceName(service);
        publishService(node);
    }

    @Override
    public void unRegister(String service, URL url, RegistryType type) {
        ZkNode node = ZkNode.UrlToZkNode(url);
        node.setRegistryType(type);
        node.setServiceName(service);
        clearService(node);
    }
}
