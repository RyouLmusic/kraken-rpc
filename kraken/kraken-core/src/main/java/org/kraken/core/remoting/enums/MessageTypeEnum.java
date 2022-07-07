package org.kraken.core.remoting.enums;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/6 17:22
 */
public enum MessageTypeEnum {
    /**
     * 普通请求
     */
    REQUEST((byte) 0x01),

    /**
     * 普通响应
     */
    RESPONSE((byte) 0x02),

    /**
     * 心跳
     */
    HEARTBEAT((byte) 0x03);
    private final byte value;

    MessageTypeEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
    public static MessageTypeEnum getEnumByValue (byte value) {
        for (MessageTypeEnum typeEnum : MessageTypeEnum.values()) {
            if (typeEnum.getValue() == value) {
                return typeEnum;
            }
        }
        return null;
    }

}
