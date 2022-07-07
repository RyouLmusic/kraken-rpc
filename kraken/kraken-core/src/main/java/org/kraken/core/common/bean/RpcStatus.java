package org.kraken.core.common.bean;


import org.kraken.core.loadbalance.LeastActiveLoadBalance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/11 15:58
 * @see LeastActiveLoadBalance
 */
public class RpcStatus {
    /**
     * address-RpcStatus
     */
    private static final ConcurrentMap<String, RpcStatus> SERVICE_STATISTICS = new ConcurrentHashMap<>();

    /**
     * address-<method,RpcStatus>
     */
    private static final ConcurrentMap<String, ConcurrentMap<String, RpcStatus>>
            METHOD_STATISTICS = new ConcurrentHashMap<>();

    /**
     * 服务方法正在执行中的数量
     */
    private final AtomicInteger active = new AtomicInteger();
    /**
     * 服务方法调用的总数量
     * 请求被限制住的
     */
    private final AtomicLong total = new AtomicLong();

    private RpcStatus() {
    }
    /**
     * 根据 URL 返回服务接口的 RpcStatus 状态
     * @param url
     * @return status
     */
    public static RpcStatus getStatus(URL url) {
        String uri = url.getAddress();
        return SERVICE_STATISTICS.computeIfAbsent(uri, key -> new RpcStatus());
    }
    /**
     * 根据 URL 返回接口中服务方法的 RpcStatus 状态
     * @param url
     * @param methodKey
     * @return status
     */
    public static RpcStatus getStatus(URL url, String methodKey) {
        String uri = url.getAddress();
        ConcurrentMap<String, RpcStatus> map = METHOD_STATISTICS.computeIfAbsent(uri, k -> new ConcurrentHashMap<>());
        // 如果不存在就new一个
        return map.computeIfAbsent(methodKey, k -> new RpcStatus());
    }

    /**
     * 服务方法执行前判断是否满足并发控制要求
     *
     * @param url
     * @param methodKey 服务方法
     * @return false：并发数已达到，挂起当前线程，等待唤醒， true： 并发数未达到，可以执行
     */
    public static boolean tryAcquire(URL url, String methodKey) {
        int max = url.getActive();
        // 若并发控制的数量 小于=0，则设置为Integer.MAX_VALUE
        max = (max <= 0) ? Integer.MAX_VALUE : max;
        // 获取这个整个 服务器地址的 状态
        RpcStatus appStatus = getStatus(url);
        // 服务器地址的下 此方法接口的 状态
        RpcStatus methodStatus = getStatus(url, methodKey);

        if (methodStatus.active.get() >= max || appStatus.active.get() >= max) {
            return false;
        }
        methodStatus.active.incrementAndGet();
        // 为整个服务器地址更新请求状态
        appStatus.active.incrementAndGet();

        return true;
    }

    /**
     *
     * @param url
     * @param methodKey
     */
    public static void release(URL url, String methodKey) {
        release(getStatus(url));
        release(getStatus(url, methodKey));
    }

    private static void release(RpcStatus status) {
        status.active.decrementAndGet();
        status.total.incrementAndGet();
    }

    public int getActive() {
        return active.get();
    }


}
