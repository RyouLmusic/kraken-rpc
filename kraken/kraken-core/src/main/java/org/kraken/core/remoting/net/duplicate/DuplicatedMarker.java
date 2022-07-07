package org.kraken.core.remoting.net.duplicate;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 17:05
 *
 * 去重处理器,集群模式下需利用SPI实现DuplicatedMarker使用redis实现
 */
public interface DuplicatedMarker {

    /**
     * 设置marker配置
     *
     * @param expireTime 过期时间
     * @param maxSize    最大存储个数
     */
    void initMarkerConfig(int expireTime, long maxSize);

    /**
     * 标记为已处理
     *
     * @param seq 请求序列号
     * @return boolean 是否重复
     * true 为重复，false 不重复
     */

    boolean mark(Long seq);

}
