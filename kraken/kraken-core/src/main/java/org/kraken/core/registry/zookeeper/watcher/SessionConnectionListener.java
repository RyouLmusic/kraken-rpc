package org.kraken.core.registry.zookeeper.watcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/3 13:14
 */
@Slf4j
public class SessionConnectionListener implements ConnectionStateListener {
    @Override
    public void stateChanged(CuratorFramework zkClient, ConnectionState connectionState) {
        System.out.println("*************************");
        if(connectionState == ConnectionState.LOST){
            log.error("zk session超时");
        }
    }
}
