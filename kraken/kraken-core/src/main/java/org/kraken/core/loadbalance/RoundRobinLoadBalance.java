package org.kraken.core.loadbalance;

import org.kraken.core.common.bean.URL;
import org.kraken.core.remoting.protocol.Request;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.kraken.core.common.utils.ConfigUtils.makeServiceKey;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/26 20:04
 * 加权轮询负载均衡
 * 每个服务器对应两个权重，分别为 weight 和 currentWeight。其中 weight 是固定的，currentWeight 会动态调整，初始值为0。
 * 当有新的请求进来时，遍历服务器列表，让它的 currentWeight 加上自身权重。
 * 遍历完成后，找到最大的 currentWeight，并将其减去权重总和，然后返回相应的服务器即可。
 *
 * 解释：https://www.jianshu.com/p/121592a06f3d，来源dubbo
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private static final int RECYCLE_PERIOD = 60000;

    protected static class WeightedRoundRobin {
        // 服务提供者权重
        private int weight;
        // 当前权重
        private final AtomicLong current = new AtomicLong(0);
        // 最后一次更新时间
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        public long increaseCurrent() {
            // current = current + weight；
            return current.addAndGet(weight);
        }
        public void sel(int total) {
            // current = current - total;
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
    // 嵌套 Map 结构，存储的数据结构示例如下：
    // {
    //     "UserService.query": {
    //         "url1": WeightedRoundRobin@123,
    //         "url2": WeightedRoundRobin@456,
    //     },
    //     "UserService.update": {
    //         "url1": WeightedRoundRobin@123,
    //         "url2": WeightedRoundRobin@456,
    //     }
    // }
    // 最外层为服务类名 + 方法名，第二层为 url 到 WeightedRoundRobin 的映射关系。
    // 这里我们可以将 url 看成是服务提供者的 id
    // <ServiceKey, <url, WeightedRoundRobin>>
    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>();


    @Override
    protected URL doSelect(List<URL> urls, Request request) {

        String key = makeServiceKey(request.getInterfaceName(), request.getMethodName(), request.getParamTypes());
        // 获取 url 到 WeightedRoundRobin 映射表，如果为空，则创建一个新的,并且塞入methodWeightMap中
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        int totalWeight = 0; // 每次的选择，totalWeight的值都是从0开始加的
        long maxCurrent = Long.MIN_VALUE;
        // 当前时间
        long now = System.currentTimeMillis();
        URL selectedUrl = null;
        WeightedRoundRobin selectedWRR = null;

        // 下面这个循环主要做了这样几件事情：
        //   1\. 遍历 urls 列表，检测当前 url 是否有
        //      相应的 WeightedRoundRobin，没有则创建
        //   2\. 检测 url 权重是否发生了变化，若变化了，
        //      则更新 WeightedRoundRobin 的 weight 字段
        //   3\. 让 current 字段加上自身权重，等价于 current += weight
        //   4\. 设置 lastUpdate 字段，即 lastUpdate = now
        //   5\. 寻找具有最大 current 的 url，以及 url 对应的 WeightedRoundRobin，
        //      暂存起来，留作后用
        //   6\. 计算权重总和
        for (URL url : urls) {
            String identifyString = url.getAddress() + url.getParam();
            int weight = getWeight(url);
            WeightedRoundRobin weightedRoundRobin = map.computeIfAbsent(identifyString, k -> {
                WeightedRoundRobin wrr = new WeightedRoundRobin();
                wrr.setWeight(weight);
                return wrr;
            });
            // url 权重不等于 WeightedRoundRobin 中保存的权重，说明权重变化了，此时进行更新
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            // 让 current 加上自身权重，等价于 current += weight，更新了动态的cur值，以便下一次选择
            long cur = weightedRoundRobin.increaseCurrent();
            // 设置 lastUpdate，表示近期更新过
            weightedRoundRobin.setLastUpdate(now);
            // 找出最大的 current，进行选择
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedUrl = url;
                selectedWRR = weightedRoundRobin;
            }

            totalWeight += weight;
        }
        // 若未更新时长超过阈值后，就会被移除掉，默认阈值为60秒。
        // 该节点可能挂了，urls 中不包含该节点，所以该节点的 lastUpdate 长时间无法被更新。
        if (urls.size() != map.size()) {
            map.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
        }
        if (selectedUrl != null) {
            // 让选中的url的weight(权重)减去totalWeight
            selectedWRR.sel(totalWeight);
            return selectedUrl;
        }
        // should not happen here
        return urls.get(0);

    }



}
