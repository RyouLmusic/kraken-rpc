package org.kraken.core.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 19:05
 *
 * 表示  网络地址 : ZK、
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class URL {
    /**
     * 协议：zk://
     */
    private String protocol;

    private String host;
    private Integer port;

    /*-------------- service configuration 在服务发布的地方进行设置 -------------*/
    /**
     * service 的version
     */
    private String version = "1";
    /**
     * service 的组
     */
    private String group;

    /**
     * service的权重
     */
    private Integer weight;

    /**
     * 服务器发布的时间戳
     *
     */
    private long timestamp;
    /**
     * 设置服务的预热时间：
     * 假设设置的权重是 100， 预热时间 10min,
     *
     * 第一分钟的时候：权重变为 (1/10)*100=10, 也就是承担 10/100 = 10% 的流量；
     * 第二分钟的时候：权重变为 (2/10)*100=20, 也就是承担 20/100 = 20% 的流量；
     * 第十分钟的时候：权重变为 (10/10)*100=100, 也就是承担 100/100 = 100% 的流量；
     * 超过十分钟之后（即 uptime>warmup，表示预热期过了，则直接返回 weight=100，不再计算）
     *
     * uptime = current - timestamp;
     */
    private int warmup;

    public String getParam() {
        return "?version=" + version +
                "&group=" + group +
                "&weight=" + weight +
                "&timestamp=" + timestamp +
                "&warmup=" + warmup;
    }

    /*---------------------------consumer configuration 接口消费的位置进行设置---------------------------*/
    /**
     * 每服务消费者每服务每方法最大并发调用数
     * 某个服务调用者  调用  同一个服务提供者的  同一个接口的同一个方法的（com.xxx.UserService.getUserName() 方法）并发数
     * 消费端进行配置的，连接同一个地址的同一个方法的最大并发数
     */
    private int active;
    /**
     * 这个接口在调用的时候，可以接受的timeout
     */
    private long timeout;
    /**
     * 请求的service版本
     */
    private String version_0;



    public URL(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public URL(URL url) {
        this.host = url.host;
        this.port = url.port;
    }



    /**
     * 获取地址 192.168.87.139:8080
     * @return 类似 192.168.87.139:8080
     */
    public String getAddress() {
        return host + ":" + port;
    }

    public InetSocketAddress getInetAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * 获取 https://192.168.87.139:8080
     * @return
     */
    /*@Override
    public String toString() {
        return protocol + "//" + host + ":" + port;
    }*/

}
