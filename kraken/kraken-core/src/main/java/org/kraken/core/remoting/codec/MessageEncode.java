package org.kraken.core.remoting.codec;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.compress.Compress;
import org.kraken.core.common.exception.EncoderException;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;
import org.kraken.core.serializer.SerializerContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * <p>
 * 自定义协议解码器
 * <p>
 * <pre>
 * 0       1       2    3           5      6        7        8   9   10  11  12              16              20
 * +---+---+-------+----+-----------+-----+---------+--------+---+---+---+---+---+---+---+---+---+---+---+---+
 * | magic |version|type|  reserve  |codec|serialize|compress|           RequestId           |      len      |
 * +---+---+-------+----+-----------+-----+---------+--------+---+---+---+---+---+---+---+---+---+---+---+---+
 * |                                                                                                         |
 * |                                         body                                                            |
 * |                                                                                                         |
 * |                                        ... ...                                                          |
 * +---------------------------------------------------------------------------------------------------------+
 * 1B magic 魔数
 * 1B version 版本
 * 1B type 消息类型
 * 2B reserve 保留字
 * 1B codec 编解码类型
 * // 1B serialize 序列化类型
 * 1B compress 压缩类型
 * 8B requestId 请求的Id
 * 4B len 消息长度
 * body MessageBody类型数据
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 17:47
 *
 * 继承
 */
public class MessageEncode extends MessageToByteEncoder<ProtocolMessage> {

    private final SerializerContext serializer;

    private final Compress compress;


    public MessageEncode() {
        this(ProtocolMessage.class, true);
    }

    /**
     * @param outboundMessageType Class
     */
    public MessageEncode(Class<? extends ProtocolMessage> outboundMessageType) {
        this(outboundMessageType, true);
    }

    /**
     * @param preferDirect preferDirect – true
     *                     if a direct ByteBuf should be tried to be used as target for the encoded messages.
     *                     If false is used it will allocate a heap ByteBuf, which is backed by an byte array.
     */
    public MessageEncode(boolean preferDirect) {
        this(ProtocolMessage.class, preferDirect);
    }

    /**
     * @param outboundMessageType Class
     * @param preferDirect preferDirect –
     */
    public MessageEncode(Class<? extends ProtocolMessage> outboundMessageType,
                         boolean preferDirect) {
        super(outboundMessageType, preferDirect);
        serializer = new SerializerContext();
        compress = Compress.getInstance();
        // TODO new Compress
    }



    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, ByteBuf out) throws Exception {

        if (msg == null || msg.getHeader() == null) {
            throw new EncoderException("The encode message is null");
        }
        ProtocolHeader header = msg.getHeader();
        Object body = msg.getBody();

        byte[] bytes = getBodyBytes(header.getType(), body);

        int len = Objects.requireNonNull(bytes).length;
        // 1B magic 魔数
        out.writeByte(Constants.MAGIC_NUMBER_2);
        // 1B version 版本
        out.writeByte(Constants.H_RPC_VERSION);
        // 1B type 消息类型
        out.writeByte(header.getType());
        // 2B reserve 保留字
        out.writeShort(header.getReserve());
        // 1B codec 编解码类型
        out.writeByte(header.getCodec());
        // // 1B serialize 序列化类型
        out.writeByte(header.getSerialize());
        // 1B compress 压缩类型
        out.writeByte(header.getCompress());
        // 8B requestId 请求的Id
        out.writeLong(header.getSeq());
        // 4B len 消息长度,
        out.writeInt(len);

        // 消息体
        out.writeBytes(bytes);
    }

    private byte[] getBodyBytes(byte msgType, Object body) {
        if (MessageTypeEnum.HEARTBEAT.getValue() == msgType) {
            // 如果是 ping、pong 心跳类型的，没有 body，直接返回0
            return new byte[0];
        }
        try {
            // 序列化
            byte[] serializerBytes = this.serializer.serializer(body);
            // 压缩
            return compress.compress(serializerBytes);
        } catch (Exception e) {
            throw new EncoderException("序列化消息体错误", e);
        }
    }


}
