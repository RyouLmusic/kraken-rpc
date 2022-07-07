package org.kraken.core.remoting.net;

import org.kraken.core.common.exception.RemotingException;

import java.net.InetSocketAddress;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/17 13:29
 *
 * 连接点
 */
public interface Endpoint {
    /*
     * get url.
     *
     * @return url
     */
//    URL getUrl();

    /**
     * get channel handler.
     *
     * @return channel handler
     */
    ChannelHandler getChannelHandler();


    /**
     * reconnect.
     */
    void reconnect(InetSocketAddress address) throws RemotingException;
}
