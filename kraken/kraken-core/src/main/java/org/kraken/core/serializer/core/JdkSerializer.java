package org.kraken.core.serializer.core;

import org.kraken.core.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/20 13:17
 *
 * 使用JDK自带的序列化方式
 * 被序列化的对象需要 实现 Serializable 接口
 */
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serializer(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        return (T) objectInputStream.readObject();
    }
}
