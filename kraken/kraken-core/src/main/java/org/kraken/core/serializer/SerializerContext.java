package org.kraken.core.serializer;

import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.extension.ExtensionLoader;
import org.kraken.core.serializer.core.HessianSerializer;
import org.kraken.core.serializer.core.JdkSerializer;
import org.kraken.core.serializer.core.KryoSerializer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/27 15:40
 *
 * 持有一个策略类（序列化方法）的引用，最终给客户端调用。
 * 调用方式：
 * SerializerContext context = new SerializerContext();
 * context.serializer(obj);
 * context.deserializer();
 */
@Slf4j
@Getter
public class SerializerContext {

    private final Serializer rpcSerializer;
    private SerializerType serializerType;

    /**
     * 默认使用 JDK 的序列化方式
     */
    public SerializerContext() {
        this.rpcSerializer = getSerializerTypeByProperties();
    }

    /**
     * 实际序列化的方法
     * @param obj 进行序列化的对象
     * @return 返回二进制数组
     * @throws Exception 抛出异常
     */
    public byte[] serializer(Object obj) throws Exception {
        return rpcSerializer.serializer(obj);
    }

    /**
     * 实际反序列化方法
     * @param bytes 需要反序列化的二进制数组
     * @param clazz 最终得到的对象实例类型
     * @param <T> 对象类型
     * @return 返回一个反序列化成功的对象
     * @throws Exception 抛出异常
     */
    public <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception {
        return rpcSerializer.deserializer(bytes, clazz);
    }

    /**
     * 获取到序列化发方式
     * @return 序列化类实例对象
     */
    private Serializer getSerializerTypeByProperties() {

        AppConfig configurationBean = ConfigUtils.getAppConfigBean();
        // 通过配置文件进行获取
        serializerType = SerializerType.valueOf(configurationBean.getSerializerType().toUpperCase(Locale.ROOT));
        Serializer serializer = null;

        // TODO 在此解决序列化的SPI扩展
        switch (serializerType) {
            case JDK: serializer = new JdkSerializer(); break;
            case HESSIAN: serializer = new HessianSerializer(); break;
            case KRYO: serializer = new KryoSerializer(); break;
            case EXTENSION: {
                ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
                serializer = extensionLoader.getExtension("protostuff");
                break;
            }

        }

        return serializer;
    }

    public enum SerializerType {

        JDK((byte) 0x01, "jdk"),
        HESSIAN((byte) 0x02, "hessian"),
        KRYO((byte) 0x03, "kryo"),
        EXTENSION((byte) 0x10, "Extension"); // 扩展的序列化方式

        private final byte key;

        private final String value;
        SerializerType(byte key, String value) {
            this.key = key;
            this.value = value;
        }

        public static String getValueByKey(byte key) {
            for (SerializerType type : SerializerType.values()) {
                if (type.getKey() == key) {
                    return type.getValue();
                }
            }
            return null;
        }

        public static byte getKeyByValue(String value) {
            for (SerializerType type : SerializerType.values()) {
                if (type.getValue().equals(value)) {
                    return type.getKey();
                }
            }
            return (byte) 0x00;
        }

        public byte getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}
