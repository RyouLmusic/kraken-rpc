package org.kraken.core.loadbalance;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.common.utils.CollectionUtils;
import org.kraken.core.remoting.protocol.Request;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/3 10:51
 */
public abstract class AbstractLoadBalance implements LoadBalance {


    @Override
    public URL select(List<URL> invokers, Request request) throws AppException {
        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return doSelect(invokers, request);
    }


    protected abstract URL doSelect(List<URL> urls, Request request);


    /**
     * Get the weight of the invoker's invocation which takes warmup time into account
     * if the uptime is within the warmup time, the weight will be reduce proportionally
     *
     * @param url    the url
     * @return weight
     */
    protected int getWeight(URL url) {
        int weight = url.getWeight();

        if (weight > 0) {
            // 服务启动时间
            long timestamp = url.getTimestamp();
            if (timestamp > 0L) {
                // 服务已运行时长
                long uptime = System.currentTimeMillis() - timestamp;
                if (uptime < 0) {
                    return 1;
                }
                // 服务预热时间，默认 DEFAULT_WARMUP = 10 * 60 * 1000 ，预热十分钟
                int warmup = url.getWarmup();
                // 如果服务运行时长小于预热时长，重新计算出预热时期的权重
                if (uptime > 0 && uptime < warmup) {
                    weight = calculateWarmupWeight((int)uptime, warmup, weight);
                }
            }
        }
        return Math.max(weight, 0);
    }

    /**
     * Calculate the weight according to the uptime proportion of warmup time
     * the new weight will be within 1(inclusive) to weight(inclusive)
     *
     * @param uptime the uptime in milliseconds
     * @param warmup the warmup time in milliseconds
     * @param weight the weight of an invoker
     * @return weight which takes warmup into account
     */
    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ( uptime / ((float) warmup / weight));
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }
}
