package org.kraken.core.common.config;

import org.kraken.core.common.annotation.PropertiesValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.compress.Compress;
import org.kraken.core.loadbalance.LoadBalance;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/20 20:07
 *
 * 一些可以进行修改的配置
 * 整个项目的配置都放在此处，通过动态代理进行读取自定义的配置
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class AppConfig {
    /**
     * 设置使用哪种方式的序列化
     *
     * jdk:
     * kryo:
     */
    @PropertiesValue(property = "kraken.serializer.type", defaultValue = "jdk")
    private String serializerType;

    /**
     * zk进行连接时候的等待时间，阻塞进行等待，
     * 如果超过此时间 还未连接成功就会抛出异常  throw new RegistryException()
     */
    @PropertiesValue(property = "kraken.zk.connected.maxWaitTime", defaultValue = "2000")
    private int zkMaxWaitTime;

    @PropertiesValue(property = "kraken.zk.is", defaultValue = "true")
    private boolean she;
    /**
     * zk连接的地址：
     * zookeeper服务端地址，多个server之间使用英文逗号分隔开
     * example: "127.0.0.1:2181,127.0.0.1:2181,127.0.0.1:2181"
     */
    @PropertiesValue(property = "kraken.zk.registry.address", defaultValue = "192.168.87.139:2181,192.168.87.139:2182,192.168.87.139:2183")
    private String registryAddress;
    /**
     * ZK注册中心的根节点路径
     */
    @PropertiesValue(property = "kraken.zk.registry.root", defaultValue = "/kraken")
    private String zkRegisterRootPath;

    /**
     * 每服务消费者每服务每方法最大并发调用数
     * 某个服务调用者  调用  同一个服务提供者的  同一个接口的同一个方法的（com.xxx.UserService.getUserName() 方法）并发数
     * 消费端进行配置的，连接同一个地址的同一个方法的最大并发数
     */
    @PropertiesValue(property = "h-rpc.reference.actives", defaultValue = "5")
    private int actives;


    @PropertiesValue(property = "kraken.server.port", defaultValue = "8002")
    private int port;
    @PropertiesValue(property = "kraken.server.class", defaultValue = "netty")
    private String serverClass;


    @PropertiesValue(property = "kraken.server.loadBalanceType", defaultValue = "ConsistentHash")
    private LoadBalance.Type loadBalanceType;

    @PropertiesValue(property = "kraken.compress.type", defaultValue = "None")
    private Compress.Type compressType;


    @PropertiesValue(property = "kraken.client.reconnectTimes", defaultValue = "2")
    private int reconnectTimes;


    public static AppConfig getAppConfig() {
        return ConfigUtils.getAppConfigBean();
    }
}
