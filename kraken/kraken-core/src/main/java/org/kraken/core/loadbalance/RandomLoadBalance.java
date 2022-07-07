package org.kraken.core.loadbalance;

import org.kraken.core.common.bean.URL;
import org.kraken.core.remoting.protocol.Request;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/7 21:26
 * 加权随机算法的具体实现：包含预热功能: 如果预热时间没有设置的话默认是10分钟
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected URL doSelect(List<URL> urls, Request request) {
        //
        int length = urls.size();

        // 是否每个 url都有相同的权重 的标志
        boolean sameWeight = true;
        // 所有url权重 落在 x轴 坐标 上的位置如：0____1____3.5_____4, 下标表示对应urls的url
        int[] weights = new int[length];
        // 所有的权重和
        int totalWeight = 0;
        // 计算总和和更新是否 每个权重都相同的标志
        for (int i = 0; i < length; i++) {
            int weight = getWeight(urls.get(i));
            // Sum
            totalWeight += weight;
            // 设置位置
            weights[i] = totalWeight;
            // 判断每一个是否和前一个相等
            if (sameWeight && totalWeight != weight * (i + 1)) {
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果所有的url权重值不都相等，从总权重值中随机取一个值，下面判断这个值对应的是哪个url
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            // Return a invoker based on the random value.
            for (int i = 0; i < length; i++) {
                // 得到的随机值(x向前的偏移量) 位于的第一个
                if (offset < weights[i]) {
                    return urls.get(i);
                }
            }
        }
        // 如果是sameWeight的，直接获取随机值返回
        return urls.get(ThreadLocalRandom.current().nextInt(length));
    }
}
