package org.kraken.core.registry.zookeeper.bean;

import com.google.common.base.Throwables;
import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.exception.RegisterException;
import org.kraken.core.common.utils.ConfigUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/2 10:58
 *
 * 对zk客户端进行管理
 * <p> ZK 节点结构 </p>
 * <pre>
 *                   O  --- root节点 : /kraken
 *                 /  \
 *               O     O  --- serviceName : com.kraken.service.HelloService
 *             /  \   |  \
 *            O   O   O   O  --- 机器角色：提供者还是调用者： providers、consumers
 *           /\  /\  /\  /\
 *          O O O O O O O O  --- 服务下的机器：URL: 192.168.87.139:2218,192.168.87.140:2218, 使用json或者其他方式将需要的信息进行存储
 *
 *          URL: , timestamp: ,
 * <pre>
 */
public class ZkManagementContext {
    /**
     * zkClient : 使用 双检锁的单例模式
     */
    private static volatile CuratorFramework zkClient;
    /**
     * 服务(注册的接口) 和 地址的 映射
     * key: com.kraken.service.HelloService; value: [192.168.87.139:2218,192.168.87.140:2218]
     */
    private static final Map<String, List<ZkNode>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    /**
     * ZkNodePath.getPath() :
     * <p>/kraken/com.kraken.service.HelloService/providers/192.168.87.139:8080</p>
     */
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    /**
     * 锁对象
     */
    private static final Object monit = new Object();
    /**
     * 使用 双检锁的单例模式 进行实现线程安全
     * @return CuratorFramework实例
     */
    public static CuratorFramework getZkClient() {
        // 如果zkClient已经实例化过了，并且处于开启的状态，就直接返回即可
        if (zkClient == null || zkClient.getState() != CuratorFrameworkState.STARTED) {
            synchronized (monit) {
                if (zkClient == null || zkClient.getState() != CuratorFrameworkState.STARTED) {

                    /*
                     * 获取配置文件中的zk地址
                     * zookeeper服务端地址，多个server之间使用英文逗号分隔开
                     * example: "127.0.0.1:2181,127.0.0.1:2181,127.0.0.1:2181"
                     */
                    String addresses = ConfigUtils.getAppConfigBean().getRegistryAddress();

                    int maxWaitTime = ConfigUtils.getAppConfigBean().getZkMaxWaitTime();

                    zkClient = CuratorFrameworkFactory.builder()
                            .connectString(addresses)
                            .connectionTimeoutMs(Constants.ZK_CONNECTION_TIME_OUT_MS)
                            .sessionTimeoutMs(Constants.ZK_SESSION_TIME_OUT_MS)
                            .retryPolicy(new ExponentialBackoffRetry(Constants.ZK_BASE_SLEEP_TIME_MS, Constants.ZK_MAX_RETRIES))
                            .build();
                    zkClient.start();


                    try {
                        // 阻塞直到连接可用，或者连接超时
                        if (!zkClient.blockUntilConnected(maxWaitTime, TimeUnit.SECONDS)) {
                            throw new RegisterException("Time out waiting to connect to ZK!");
                        }
                    } catch (InterruptedException e) {
                        throw new RegisterException(Throwables.getStackTraceAsString(e));
                    }
                }
            }
        }

        return zkClient;
    }

    /**
     * 返回
     * @param serviceName 服务名称
     * @return 返回
     */
    public static List<ZkNode> getNodesFromServiceAddressMap(String serviceName) {
        return SERVICE_ADDRESS_MAP.getOrDefault(serviceName, new ArrayList<>());
    }

    /**
     * 添加
     * @param serviceName 服务名称
     * @param node 节点
     */
    public static void addServiceAddressMapNode(String serviceName, ZkNode node) {
        getNodesFromServiceAddressMap(serviceName).add(node);
    }

    public static void putAddressNode(String serviceName, List<ZkNode> nodes) {
        SERVICE_ADDRESS_MAP.put(serviceName, nodes);
    }

    /**
     * 是否为空
     * @param serviceName 服务名称
     * @return boolean
     */
    private static boolean zkNodesIsEmpty(String serviceName) {
        return getNodesFromServiceAddressMap(serviceName).isEmpty();
    }
    /**
     * 是否已经注册了 某服务
     * @param serviceName 服务名称
     * @return boolean
     */
    public static boolean isContainsService(String serviceName) {
        return SERVICE_ADDRESS_MAP.containsKey(serviceName);
    }



    /**
     * 查看path是否已经添加
     * @param path 路径
     * @return boolean
     */
    public static boolean pathIsRegistered(String path) {
        return REGISTERED_PATH_SET.contains(path);
    }

    /**
     * 进行添加
     * @param path 路径
     */
    public static void addRegisteredPathSet(String path) {
        REGISTERED_PATH_SET.add(path);
    }

    /**
     * 迭代处理 RegisteredPathSet 里面的元素
     * @param function 传入 方法
     */
    public static void forEachRegisteredPathSet(Consumer<String> function) {
        // stream().parallel() : 并行流：可用提高CPU的利用率
        REGISTERED_PATH_SET.parallelStream().forEach(function);
    }

    public static int getRegisteredPathSetSize() {
        return REGISTERED_PATH_SET.size();
    }
}
