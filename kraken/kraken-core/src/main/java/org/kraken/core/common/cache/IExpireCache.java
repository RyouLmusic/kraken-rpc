package org.kraken.core.common.cache;

import java.util.Set;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 15:28
 */
public interface IExpireCache<K, V> {

    V put(K key, V value);

    V get(K key);

    Set<K> keySet();

    void invalidate(K key);

}
