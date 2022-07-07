package com.serializer;

import org.kraken.core.serializer.Serializer;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/4/5 10:44
 */
public class ProtobufSerializer implements Serializer {
    @Override
    public byte[] serializer(Object obj) throws Exception {
        return new byte[0];
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception {
        return null;
    }
}
