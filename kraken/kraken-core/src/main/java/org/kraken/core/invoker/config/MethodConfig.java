package org.kraken.core.invoker.config;

import lombok.Data;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/11 23:23
 * TODO spring 通过注释来实现
 */
@Data
public class MethodConfig implements Config {
    /**
     * 每服务消费者每服务每方法最大并发调用数
     * 某个服务调用者  调用  同一个服务提供者的  同一个接口的同一个方法的（com.xxx.UserService.getUserName() 方法）并发数
     * 消费端进行配置的，连接同一个地址的同一个方法的最大并发数
     * Integer：active
     */
    private int active;

    /**
     * 这个接口在调用的时候，可以接受的timeout
     */
    private long timeout;
    /**
     * 请求的service版本
     */
    private String version;
}
