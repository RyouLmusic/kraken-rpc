package org.kraken.core.registry;

import org.kraken.core.common.exception.RegisterException;
import org.kraken.core.registry.zookeeper.bean.ZkManagementContext;
import org.kraken.core.registry.zookeeper.watcher.SessionConnectionListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 15:53
 * zk服务的抽象类
 */
@Slf4j
public abstract class ZkService {

    /**
     * zk的地址  集群的话，应该是 192.168.87.139:2188,192.168.87.140:2188
     */
    protected String address;

    /**
     * 初始化，并且得到地址
     */
    public void initRegistry() {
        //
        SessionConnectionListener sessionConnectionListener = new SessionConnectionListener();
        try {
            CuratorFramework zkClient = ZkManagementContext.getZkClient();
            zkClient.getConnectionStateListenable().addListener(sessionConnectionListener);
        } catch (RegisterException e) {
            throw new RegisterException("zk服务初始哈失败", e);
        }
    }

    /**
     * 关闭服务，并且清除ZKManagementContext中的内容
     */
    public void destroyRegister() {

    }
}
