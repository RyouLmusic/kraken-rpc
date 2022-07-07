package org.kraken.core.remoting.protocol;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.remoting.enums.CodecTypeEnum;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/6 14:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProtocolHeader implements Serializable {
    /**
     * 魔数：
     * 0x80、0x81 	帧头
     */
    private byte magic;
    /**
     * 版本号
     */
    private byte version;
    /**
     * 消息类型: {@link MessageTypeEnum}
     */
    private byte type;

    /**
     * 保留标志：一个字节 以备扩展
     */
    private short reserve;

    /**
     * 编解码类型
     * {@link CodecTypeEnum}
     */
    private byte codec;
    /**
     * 压缩类型
     */
    private byte compress;
    /**
     * 序列化类型
     */
    private byte serialize;
    /**
     * 序号，指令唯一标识
     * 进行去重 seq
     */
    private long seq;
    /**
     * 消息体长度
     * 消息体 Object date length
     * 可以解决粘包等问题
     */
    private int len;


    public static ProtocolHeader builtByReqHeader(ProtocolHeader header) {

        header.setVersion(Constants.H_RPC_VERSION);
        header.setMagic(Constants.MAGIC_NUMBER_2);
        header.setType(MessageTypeEnum.RESPONSE.getValue());
        header.setLen(0);
        return header;
    }

}
