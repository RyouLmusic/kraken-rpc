package org.kraken.core.remoting.net.duplicate;


import org.kraken.core.common.cache.IExpireCache;
import org.kraken.core.common.cache.ScheduleEvictExpireCache;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 16:58
 * 内存级别去重处理
 */
public class DefaultDuplicateMarker implements DuplicatedMarker {

    private IExpireCache<Long, Boolean> expireCache;

    @Override
    public void initMarkerConfig(int expireTime, long maxSize) {
        expireCache = new ScheduleEvictExpireCache<>(expireTime, TimeUnit.SECONDS, maxSize);
    }

    @Override
    public boolean mark(Long seq) {
        return expireCache.put(seq, Boolean.TRUE) != null;
    }
}
