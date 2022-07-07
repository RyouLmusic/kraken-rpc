package org.kraken.core.remoting.net;

import org.kraken.core.common.exception.RemotingException;
import io.netty.channel.Channel;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/17 13:31
 *
 * 消息操作, TODO SPI //@SPI(scope = ExtensionScope.FRAMEWORK)
 */
public interface ChannelHandler {

    /**
     * on channel connected.
     *
     * @param channel channel.
     */
    void connected(Channel channel) throws RemotingException;

    /**
     * on channel disconnected.
     *
     * @param channel channel.
     */
    void disconnected(Channel channel) throws RemotingException;



    /**
     * on exception caught.
     *
     * @param channel   channel.
     * @param exception exception.
     */
    void caught(Channel channel, Throwable exception) throws RemotingException;
}
