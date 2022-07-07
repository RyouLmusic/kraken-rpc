package org.kraken.core.invoker.limiting;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/15 11:04
 */
public class RateLimiterSimpleWindow implements RateLimiter {

    // 阈值
    private static Integer QPS = 5;
    // 时间窗口（毫秒）
    private final static long TIME_WINDOWS = 1000;
    // 计数器
    private final static AtomicInteger REQ_COUNT = new AtomicInteger();

    private static long START_TIME = System.currentTimeMillis();

    public synchronized boolean tryAcquire() {
        if ((System.currentTimeMillis() - START_TIME) > TIME_WINDOWS) {
            REQ_COUNT.set(0);
            START_TIME = System.currentTimeMillis();
        }
        return REQ_COUNT.incrementAndGet() <= QPS;
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiterSimpleWindow simpleWindow = new RateLimiterSimpleWindow();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                LocalTime now = LocalTime.now();
                if (!simpleWindow.tryAcquire()) {
                    System.out.println(now + " 被限流");
                } else {
                    System.out.println(now + " 做点什么");
                }
            }, "" + i);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        /*for (int i = 0; i < 10; i++) {
            Thread.sleep(250);
            LocalTime now = LocalTime.now();
            if (!simpleWindow.tryAcquire()) {
                System.out.println(now + " 被限流");
            } else {
                System.out.println(now + " 做点什么");
            }
        }*/
    }
}
