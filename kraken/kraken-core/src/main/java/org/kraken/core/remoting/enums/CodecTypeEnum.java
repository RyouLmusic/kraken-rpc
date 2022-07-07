package org.kraken.core.remoting.enums;

import lombok.AllArgsConstructor;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/6 17:31
 *
 * 编解码类型
 */
@AllArgsConstructor
public enum CodecTypeEnum {

    HESSIAN((byte) 0x01),
    JDK((byte) 0x02),
    KRYO((byte) 0x03);

    private final byte value;
}
