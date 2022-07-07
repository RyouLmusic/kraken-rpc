package org.kraken.core.loadbalance;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.remoting.protocol.Request;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/2 19:23
 */
public interface LoadBalance {

    // TODO 添加其他方式
    enum Type {
        LeastActive, Random, RoundRobin, ConsistentHash;
    }

    LeastActiveLoadBalance leastActive = new LeastActiveLoadBalance();
    RandomLoadBalance random = new RandomLoadBalance();
    ConsistentHashLoadBalance consistentHash = new ConsistentHashLoadBalance();
    RoundRobinLoadBalance roundRobin = new RoundRobinLoadBalance();
    /**
     * select one invoker in list.
     *
     * @param invokers   urls.
     * @param request invocation.
     * @return selected invoker.
     */
    URL select(List<URL> invokers, Request request) throws AppException;

    static LoadBalance getInstance() {

        AppConfig appConfig = AppConfig.getAppConfig();
        switch (appConfig.getLoadBalanceType()) {
            case LeastActive :

                return leastActive;
            case ConsistentHash :

                return consistentHash;
            case RoundRobin :

                return roundRobin;
            default :

                return random;
        }
    }



}
