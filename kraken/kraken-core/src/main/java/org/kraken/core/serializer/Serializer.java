package org.kraken.core.serializer;

import org.kraken.core.extension.SPI;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/20 13:14
 *
 * 提供RPC的序列化接口
 * 其他实现都是基于此接口的实现
 */
@SPI
public interface Serializer {

    /**
     * 序列化操作
     * @param obj 进行序列化操作的对象
     * @return 返回的二进制数组
     * @throws Exception 抛出异常
     */
    byte[] serializer(Object obj) throws Exception;

    /**
     * @param bytes 反序列化操作的对象数据
     * @param clazz 进行反序列化操作的对象类型
     * @param <T> 返回的类型
     * @return
     * @throws Exception
     */
    <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception;
}
