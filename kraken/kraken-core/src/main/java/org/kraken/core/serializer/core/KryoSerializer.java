package org.kraken.core.serializer.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import org.kraken.core.serializer.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/22 17:53
 *
 * Kryo对象不是线程安全的，可以使用ThreadLocal或池来获取（本文使用池获取）
 */
public class KryoSerializer implements Serializer {
    private final static Kryo kryo = new Kryo();
    // Configure the Kryo instance.
    /**
     *  Because Kryo is not thread safe and constructing and configuring a Kryo instance is relatively expensive,
     * in a multithreaded environment ThreadLocal or pooling might be considered.
     * Kryo kryo = kryoInstances.get();
     */
    private final ThreadLocal<Kryo> kryoInstances = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        /* 而StdInstantiatorStrategy在是依据JVM version信息及JVM vendor信息创建对象的，可以不调用对象的任何构造方法创建对象。
         * 那么例如碰到ArrayList这样的对象时候，就会出问题。
         * 解决方案很简单，就如框架中代码写的一样，显示指定实例化器，首先使用默认无参构造策略DefaultInstantiatorStrategy
         * 若创建对象失败再采用StdInstantiatorStrategy。
         */
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        return kryo;
    });

    /**
     * For pooling, Kryo provides the Pool class which can pool Kryo, Input, Output,
     * or instances of any other class.
     *
     * Kryo kryo = kryoPool.obtain();
     * // Use the Kryo instance here.
     * kryoPool.free(kryo);
     */
    Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        protected Kryo create () {
            Kryo kryo = new Kryo();
            // Configure the Kryo instance.

            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            return kryo;
        }
    };


    public byte[] serializerThreadUnsafe(Object obj) throws Exception {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

        Output output = new Output(byteArray);
//        Kryo kryo = new Kryo();
        kryo.register(obj.getClass());
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
        return byteArray.toByteArray();
    }

    public <T> T deSerializerThreadUnsafe(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArray);
//        Kryo kryo = new Kryo();
        kryo.register(clazz);

        input.close();
        return kryo.readObject(input, clazz);
    }

    /**
     * 这两个方法是线程安全的
     * @param obj
     * @return
     * @throws Exception
     */
    @Override
    public byte[] serializer(Object obj) throws Exception {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

        Output output = new Output(byteArray);
        Kryo kryo = kryoInstances.get();
        kryo.register(obj.getClass());
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
        return byteArray.toByteArray();
    }

    /**
     * Kryo的反序列化
     * @param bytes 反序列化操作的对象数据
     * @param clazz 进行反序列化操作的对象类型
     * @param <T> 返回的类型
     * @return 返回对象
     * @throws Exception 可能抛出的异常
     */
    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArray);
        Kryo kryo = kryoInstances.get();
        kryo.register(clazz);

        input.close();
        return kryo.readObject(input, clazz);
    }
}
