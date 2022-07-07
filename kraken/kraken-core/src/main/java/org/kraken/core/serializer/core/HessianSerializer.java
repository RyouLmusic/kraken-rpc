package org.kraken.core.serializer.core;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.kraken.core.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/22 17:13
 *
 * Hessian是一个支持跨语言传输的二进制序列化协议，相对于Java默认的序列化机制来说，
 * Hessian具有更好的性能和易用性，而且支持多种不同的语言
 * 注：Hessian序列化包含 BigDecimal 字段的对象时会导致其值一直为0
 *
 * 被序列化的对象需要 实现 Serializable 接口
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serializer(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        hessian2Output.writeObject(obj);
        hessian2Output.flush();
        hessian2Output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);

        return (T) hessian2Input.readObject(clazz);
    }



}


