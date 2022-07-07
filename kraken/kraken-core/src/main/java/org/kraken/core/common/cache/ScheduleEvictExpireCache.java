package org.kraken.core.common.cache;


import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 14:23
 */
@Slf4j
public class ScheduleEvictExpireCache<K, V> implements IExpireCache<K, V> {
    private static final long UNSET_INT = Long.MAX_VALUE;
    /**
     * 过期时间
     */
    private final long expireTimeMs;
    /**
     *
     */
    private final long bulkTimeMs;
    private Node<K> headNode, lastNode;
    private final Map<K, V> resultMap = new ConcurrentHashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private ScheduledExecutorService schedule;
    private final long maxSize;
    private static ThreadFactory threadFactory;

    public ScheduleEvictExpireCache(int expireTime, TimeUnit unit) {
        this(expireTime, unit, 10, UNSET_INT);
    }

    public ScheduleEvictExpireCache(int expireTime, TimeUnit unit, long maxSize) {
        this(expireTime, unit, 10, maxSize);
    }

    public ScheduleEvictExpireCache(int expireTime, TimeUnit unit, int bulkSize, long maxSize) {
        if (expireTime <= 0) {
            throw new RuntimeException("expireTime can't be low zero.");
        }

        this.expireTimeMs = unit.toMillis(expireTime);
        this.maxSize = maxSize;
        if (bulkSize <= 0) bulkSize = 10;
        this.bulkTimeMs = this.expireTimeMs / bulkSize;
        initScheduleExecutor();

        startSchedule();
    }

    /**
     * 初始化进行去重的守护线程
     */
    private void initScheduleExecutor() {
        if (threadFactory == null) {
            synchronized (ScheduleEvictExpireCache.class) {
                if (threadFactory == null) {
                    threadFactory = new ThreadFactory() {
                        private final AtomicInteger threadNumber = new AtomicInteger(1);
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Thread thread = new Thread(runnable, "kraken-cache-expire-thread-" + threadNumber.getAndIncrement());
                            thread.setDaemon(true);
                            return thread;
                        }
                    };
                }
            }
        }
        schedule = Executors.newScheduledThreadPool(1, threadFactory);
    }

    /**
     * 开启去重守护线程
     */
    private void startSchedule() {
        schedule.scheduleAtFixedRate(() -> {
            try {
                evict();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, expireTimeMs, bulkTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 自动过期, 从头结点开始移除，与添加不冲突，可不需要进行加锁.
     */
    private void evict() {
        //可做去重作用.
        try {
            this.writeLock.lock();
            while (headNode != null
                    && (headNode.isExpire() || resultMap.size() > maxSize)) {
                for (K key : headNode.keys) {
                    resultMap.remove(key);
                }
                headNode = headNode.next;
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        /*
         * 此部分无需加锁.
         */
        V result = resultMap.put(key, value);
        if (result != null) {
            //覆盖修改，过期时间还是按原来的计算
            return result;
        }

        /*
         * 变更node则才需要加锁.
         */
        try {
            this.writeLock.lock();
            if (headNode == null) {
                headNode = lastNode = new Node<>(key);
            } else {
                if (lastNode.isInCurInterval()) {
                    lastNode.addKey(key);
                } else {
                    lastNode = lastNode.next = new Node<>(key);
                }
            }
        } finally {
            this.writeLock.unlock();
        }
        return null;
    }

    @Override
    public V get(K key) {
        try {
            this.readLock.lock();
            return resultMap.get(key);
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        try {
            this.readLock.lock();
            return resultMap.keySet();
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void invalidate(K key) {
        try {
            this.writeLock.lock();
            resultMap.remove(key);
        } finally {
            this.writeLock.unlock();
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    /**
     * 将需要去重的对象抽象成节点
     * @param <K>
     */
    class Node<K> {
        /**
         * 时间戳
         */
        private final long timestamp;
        private final Set<K> keys = new LinkedHashSet<>();
        private Node<K> next;

        private Node(K key) {
            this.timestamp = now();
            addKey(key);
        }

        private void addKey(K key) {
            keys.add(key);
        }

        private boolean isExpire() {
            return now() - timestamp > expireTimeMs;
        }

        private boolean isInCurInterval() {
            return now() - timestamp < bulkTimeMs;
        }
    }
}
