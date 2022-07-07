package org.kraken.core.remoting.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/6 20:37
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProtocolMessage implements Serializable {

    /**
     * 消息头
     */
    private ProtocolHeader header;
    /**
     * 消息体
     * {@link Request}
     * {@link Response}
     */
    private Object body;

}
