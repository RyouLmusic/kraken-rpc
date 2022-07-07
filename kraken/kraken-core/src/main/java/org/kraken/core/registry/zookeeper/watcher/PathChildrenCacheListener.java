package org.kraken.core.registry.zookeeper.watcher;

import org.kraken.core.registry.zookeeper.bean.ZkManagementContext;
import org.kraken.core.registry.zookeeper.bean.ZkNode;
import org.kraken.core.registry.zookeeper.bean.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/3 13:37
 *
 * 节点路径变化监听器
 */
@Slf4j
public class PathChildrenCacheListener implements org.apache.curator.framework.recipes.cache.PathChildrenCacheListener {
    private String servicePath;
    private String serviceName;
    public PathChildrenCacheListener() {}
    public PathChildrenCacheListener(String servicePath, String serviceName) {
        this.servicePath = servicePath;
        this.serviceName = serviceName;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {

        List<ZkNode> results = new ArrayList<>();
        ZkUtils.dfs(curatorFramework, servicePath, results);
        ZkManagementContext.putAddressNode(serviceName, results);

        // TODO 进行界面显示
        ChildData data = event.getData();
        switch (event.getType()) {
            case CHILD_ADDED:

                log.info("子节点增加, path={}, data={}",
                        data.getPath(), new String(data.getData(), StandardCharsets.UTF_8));

                break;
            case CHILD_UPDATED:
                log.info("子节点更新, path={}, data={}",
                        data.getPath(), new String(data.getData(), StandardCharsets.UTF_8));
                break;
            case CHILD_REMOVED:
                log.info("子节点删除, path={}, data={}",
                        data.getPath(), new String(data.getData(), StandardCharsets.UTF_8));
                // TODO 进行宕机重连

                break;
            default:
                break;

        }
    }
}
