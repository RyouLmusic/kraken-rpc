package org.kraken.core.registry.zookeeper.bean;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.exception.RegisterException;
import org.kraken.core.common.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 15:54
 * ZK服务发工具类
 */

@Slf4j
public class ZkUtils {

    private static final String ZK_REGISTER_ROOT_PATH = ConfigUtils.getAppConfigBean().getZkRegisterRootPath();

    /**
     * 创建(添加) 持久节点
     * @param zkClient 客户端
     * @param node 节点里获取path
     */
    private static void createPersistentNode(CuratorFramework zkClient, ZkNode node) {
        String path = node.getPath();
        try {
            if (ZkManagementContext.pathIsRegistered(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("此节点已经存在, 节点路径为：{}", node.getPath());
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("持久节点(发布服务)创建成功，节点(服务名称)路径为：{}", path);
            }
            // 添加到合集里面，防止重复创建
            ZkManagementContext.addRegisteredPathSet(path);
            // TODO zk Map 还未更新
        } catch (Exception e) {
            throw new RegisterException("zk创建持久节点出现异常...", e);
        }
    }

    /**
     * 创建  提供者的 节点
     * @param zkClient
     * @param node
     */
    public static void createPersistentProviderNode(CuratorFramework zkClient, ZkNode node) {
        node.setRegistryType(RegistryType.PROVIDER);
        createPersistentNode(zkClient, node);
    }

    /**
     * 创建 消费者的 节点
     * @param zkClient
     * @param node
     */
    public static void createPersistentConsumerNode(CuratorFramework zkClient, ZkNode node) {
        node.setRegistryType(RegistryType.CONSUMER);
        createPersistentNode(zkClient, node);
    }

    /**
     * 将URL地址下的节点 节点下的 所有服务都清除
     * @param zkClient zk连接
     * @param node 需要删除操作的机器的地址
     */
    public static void clearRegistryByAddress(CuratorFramework zkClient, ZkNode node) {
        AtomicInteger count = new AtomicInteger();
        ZkManagementContext.forEachRegisteredPathSet(path -> {

            if (path.split("/")[4].startsWith(node.getAddress())) {
                try {
                    zkClient.delete().forPath(path);
                    count.getAndIncrement();
                } catch (Exception e) {
                    throw new RegisterException("删除已经发布的服务操作中抛出异常", e);
                }
            }
        });
        if (count.get() == 0) {
            log.info("此[{}]机器还未进行注册" , node.getAddress());
        } else {
            log.info("成功删除[{}]机器下的所有服务" , node);
        }
    }

    /**
     * 删除集群zk下的所有机器的 名称为serviceName的服务
     * @param zkClient 连接
     * @param serviceName 服务名称
     */
    public static void clearRegistryByServiceName(CuratorFramework zkClient, String serviceName) {

        ZkManagementContext.forEachRegisteredPathSet((path) -> {
            if (path.contains(serviceName)) {
                try {
                    zkClient.delete().forPath(path);
                } catch (Exception e) {
                    throw new RegisterException("删除已经发布的服务操作中抛出异常", e);
                }
            }
        });
    }



    /**
     * 获取节点下的所有路径, 带本地内存级别缓存
     * @param service rpc service name
     * @return All child nodes under the specified node
     */
    public static List<ZkNode> getChildrenNodes(CuratorFramework zkClient, String service,RegistryType type) {
        // 如果内存中可获取到，直接返回
        if (ZkManagementContext.isContainsService(service)) {
            return ZkManagementContext.getNodesFromServiceAddressMap(service);
        }
        // 内存中没有获取到，通过zk连接进行获取
        List<ZkNode> result = new ArrayList<>();

        String path = ZK_REGISTER_ROOT_PATH + Constants.SEPARATOR + service + Constants.SEPARATOR + type.getValue();
        try {
            if (zkClient.checkExists().forPath(path) != null) {
                dfs(zkClient, path, result);
            }

            ZkManagementContext.putAddressNode(service, result);
            // 注册到观察 节点变化的时候进行更改 TODO 其他地方也要监听
            registerWatcher(zkClient, service, type);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", path);
        }
        return result;
    }

    /**
     * 返回 service 节点下面的 provider 节点的所有子节点：即为 所有注册为  提供  此service服务的机器 address信息
     * @param zkClient zkClient
     * @param service 服务名称
     * @return 返回节点信息集合
     */
    public static List<ZkNode> getProviderChildrenNodes(CuratorFramework zkClient, String service) {
        return getChildrenNodes(zkClient, service, RegistryType.PROVIDER);
    }

    /**
     * 返回 service 节点下面的 consumer 节点的所有子节点：即为 所有注册为  消费(订阅)  此service服务的机器 address信息
     * @param zkClient zkClient
     * @param service 服务名称
     * @return 返回节点信息集合
     */
    public static List<ZkNode> getConsumerChildrenNodes(CuratorFramework zkClient, String service) {
        return getChildrenNodes(zkClient, service, RegistryType.CONSUMER);
    }

    /**
     * 更新内存里面的ZK合集
     * SERVICE_ADDRESS_MAP,
     * REGISTERED_PATH_SET
     */
    public static void updateZkManagementData() {
        List<ZkNode> zkNodes = new ArrayList<>();
        CuratorFramework zkClient = ZkManagementContext.getZkClient();
        try {
            // 从根节点开始 递归获取
            dfs(zkClient, ZK_REGISTER_ROOT_PATH, zkNodes);

            zkNodes.stream().parallel().forEach(node -> {
                String serviceName = node.getServiceName();
                ZkManagementContext.addServiceAddressMapNode(serviceName, node);
                ZkManagementContext.addRegisteredPathSet(node.getPath());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 通过一个 路径的前部分 例如： /kraken/com.kraken.service.HelloService
     * 获取所有底下的所有子节点
     * @param zkClient 客户端
     * @param path 节点路径
     * @param zkNodes 节点集合
     * @throws Exception 抛出异常
     */
    public static void dfs(CuratorFramework zkClient, String path, List<ZkNode> zkNodes) throws Exception {
        if (!path.startsWith(Constants.SEPARATOR)) {
            path = Constants.SEPARATOR + path;
        }
        if (zkClient.getChildren().forPath(path).isEmpty()) {
            zkNodes.add(new ZkNode(path));
            return;
        }
        String finalPath = path;
        zkClient.getChildren().forPath(path).forEach(p -> {
            try {
                p = finalPath + Constants.SEPARATOR + p;
                dfs(zkClient, p, zkNodes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 注册监听，监听节点下的变化
     * @param zkClient zk连接
     * @param service 服务名称
     * @param type 节点类型
     * @throws Exception
     */
    private static void registerWatcher(CuratorFramework zkClient, String service, RegistryType type) throws Exception {
        String path = ZK_REGISTER_ROOT_PATH + Constants.SEPARATOR + service + Constants.SEPARATOR + type.getValue();
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, path, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {

            List<ZkNode> zkNodes = new ArrayList<>();
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(path);
            serviceAddresses.forEach(ip -> {
                String nodePath = path + Constants.SEPARATOR + ip;
                zkNodes.add(new ZkNode(nodePath));
            });


            ZkManagementContext.putAddressNode(service, zkNodes);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.getListenable().addListener(
                new org.kraken.core.registry.zookeeper.watcher.PathChildrenCacheListener(path, service));

        pathChildrenCache.start();
    }

    /*private static ThreadPoolExecutor zkWatcherExecutor =
            new ThreadPoolExecutor(10, 100, 60L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000),
                    new ThreadPoolExecutor.AbortPolicy());*/

    private static Map<String, PathChildrenCache> pathListenerCache = new ConcurrentHashMap<>();

    /**
     * 对于某个节点进行监听
     * @param zkClient
     * @param listener
     * @param path
     */
    public static void registerWatcher(CuratorFramework zkClient, PathChildrenCacheListener listener, String path) {
        String key = zkClient.toString() + "$" + path;
        PathChildrenCache pathChildrenCache = pathListenerCache.getOrDefault(key, new PathChildrenCache(zkClient, path, true));
        pathListenerCache.putIfAbsent(key, pathChildrenCache);

        pathChildrenCache.getListenable().addListener(listener);
    }

}
