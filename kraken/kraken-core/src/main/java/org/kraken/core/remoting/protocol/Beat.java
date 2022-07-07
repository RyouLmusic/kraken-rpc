package org.kraken.core.remoting.protocol;

import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.common.utils.IdGeneratorUtil;
import org.kraken.core.compress.Compress;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import org.kraken.core.serializer.SerializerContext;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/20 17:31
 */
public class Beat {
    static final AppConfig config = ConfigUtils.getAppConfigBean();

    public static ProtocolMessage ping() {

        String body = "PING";
        ProtocolMessage ping = new ProtocolMessage();

        ping.setHeader(header());
        ping.setBody(body);

        return ping;
    }

    public static ProtocolMessage pong() {
        String body = "PONG";
        ProtocolMessage pong = new ProtocolMessage();
        pong.setHeader(header());
        pong.setBody(body);

        return pong;
    }

    private static ProtocolHeader header() {
        String type = config.getSerializerType();
        byte codec = SerializerContext.SerializerType.getKeyByValue(type);
        Compress.Type compressType = config.getCompressType();
        return ProtocolHeader
                .builder()
                .type(MessageTypeEnum.HEARTBEAT.getValue())
                .seq(IdGeneratorUtil.getId())
                .serialize(codec)
                .compress(compressType.getCode())
                .reserve((short) 0)
                .codec(codec)
                .build();
    }
}
