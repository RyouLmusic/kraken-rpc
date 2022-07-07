package org.kraken.spring.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 9:45
 * 用于生产端
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Service
public @interface KrakenService {

    /**
     * service 的version
     */
    String version() default  "1";
    String group() default "";
    /**
     * service的权重
     */
    int weight() default 5;
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
    int warmup() default 10;
}
