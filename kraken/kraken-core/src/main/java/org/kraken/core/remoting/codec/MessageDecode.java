package org.kraken.core.remoting.codec;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.exception.DecoderException;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.compress.Compress;
import org.kraken.core.remoting.protocol.MessageBody;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;
import org.kraken.core.serializer.SerializerContext;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;


/**
 * <p>
 * 自定义协议解码器
 * <p>
 * <pre>
 *    0       1       2    3           5      6        7        8   9   10  11  12              16              20
 *   +---+---+-------+----+-----------+-----+---------+--------+---+---+---+---+---+---+---+---+---+---+---+---+
 *   | magic |version|type|  reserve  |codec|serialize|compress|           RequestId           |      len      |
 *   +---+---+-------+----+-----------+-----+---------+--------+---+---+---+---+---+---+---+---+---+---+---+---+
 *   |                                                                                                         |
 *   |                                         body                                                            |
 *   |                                                                                                         |
 *   |                                        ... ...                                                          |
 *   +---------------------------------------------------------------------------------------------------------+
 *   1B magic 魔数
 *   1B version 版本
 *   1B type 消息类型
 *   2B reserve 保留字
 *   1B codec 编解码类型
 *   // 1B serialize 序列化类型
 *   1B compress 压缩类型
 *   8B requestId 请求的Id
 *   4B len 消息长度
 *   body MessageBody类型数据
 * </pre>
 * LengthFieldBasedFrameDecoder 自定义长度解码器解决TCP黏包问题。
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 11:22
 */
@Slf4j
public class MessageDecode extends LengthFieldBasedFrameDecoder {

    /**
     * 序列化器
     */
    private final SerializerContext serializer;
    private final Compress compress;
    private final AppConfig appConfig;

    public MessageDecode() {
        super(
                Constants.MAX_FRAME_LENGTH, // 最大的长度，如果超过，会直接丢弃
                Constants.LENGTH_FIELD_OFFSET,
                Constants.LENGTH_FIELD_LENGTH,
                Constants.LENGTH_ADJUSTMENT,
                Constants.INITIAL_BYTES_TO_STRIP);
        serializer = new SerializerContext();
        compress = Compress.getInstance();
        appConfig = ConfigUtils.getAppConfigBean();
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= Constants.HEADER_SIZE) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode 错误.", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private ProtocolMessage decodeFrame(ByteBuf frame) {

        //校验魔数
        byte magic = checkMagicNumber(frame);

        byte version = checkVersion(frame);
        // 解析消息头
        ProtocolHeader header = parseHeader(frame, magic, version);

        // 如果消息体的长度没有没有达到
        if (frame.readableBytes() != header.getLen()) {
            return ProtocolMessage.builder().header(header).build();
        }

        // 消息类型是 心跳：直接返回
        if (header.getType() == MessageTypeEnum.HEARTBEAT.getValue()) {
            return ProtocolMessage.builder().header(header).build();
        }

        // 获取消息体
        byte[] bytes = new byte[header.getLen()];
        frame.readBytes(bytes);

        //


        try {
            byte[] decompress = compress.decompress(bytes);
            // 反序列化
            MessageBody body = serializer.deserializer(decompress, MessageBody.class);

            return ProtocolMessage.builder()
                    .header(header)
                    .body(body)
                    .build();
        } catch (Exception e) {
            log.error("解码失败", e);
            e.printStackTrace();
        }

        return null;
    }

    private ProtocolHeader parseHeader(ByteBuf frame, byte magic, byte version) {

        // magic |version|type|  reserve  |codec|serialize|compress| RequestId |len|
        byte msgType = frame.readByte();
        short reserve = frame.readShort();
        byte codec = frame.readByte();
        byte serialize = frame.readByte();
        // 序列化方式不支持
        if (SerializerContext.SerializerType.getValueByKey(serialize) == null) {
            throw new DecoderException("不支持的序列化方式");
        }
        // 序列化方式是否为相同的序列化配置
        if (!Objects.equals(SerializerContext.SerializerType.getValueByKey(serialize), appConfig.getSerializerType())) {
            throw new DecoderException("序列化方式不同");
        }
        byte compressCode = frame.readByte();

        Compress.Type compressType = Compress.Type.getTypeByCode(compressCode);
        if (!Objects.equals(appConfig.getCompressType(), compressType)) {
            throw new DecoderException("压缩方式不同");
        }

        long requestId = frame.readLong();
        int len = frame.readInt();

        return ProtocolHeader.builder()
                .magic(magic)
                .version(version)
                .type(msgType)
                .reserve(reserve)
                .codec(codec)
                .serialize(serialize)
                .compress(compressCode)
                .seq(requestId)
                .len(len).build();
    }

    /**
     * 检查版本
     * @param frame buffer
     */
    private byte checkVersion(ByteBuf frame) {
        byte version = frame.readByte();
        if (version != Constants.H_RPC_VERSION) {
            throw new DecoderException("未知Rpc版本" + version);
        }
        return version;
    }

    /**
     * 检查魔数
     * @param frame buffer
     */
    private byte checkMagicNumber(ByteBuf frame) {
        byte magic = frame.readByte();
        if (magic != Constants.MAGIC_NUMBER_1 && magic != Constants.MAGIC_NUMBER_2) {
            throw new DecoderException("未知魔数" + magic);
        }
        return magic;
    }

}
