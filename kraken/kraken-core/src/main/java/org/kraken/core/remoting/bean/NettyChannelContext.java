package org.kraken.core.remoting.bean;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 22:31
 * 对客户端的channel管理
 */
public class NettyChannelContext {

    /**
     * 地址--连接的channel
     * 192.168.87.139:2558 -- Channel
     */
    private final static Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();


    protected Channel getChannelByAddress(String address) {
        return CHANNEL_MAP.get(address);
    }

    protected void removeChannelIfDisconnected(Channel channel) {
        if (channel != null && channel.isOpen()) {
            CHANNEL_MAP.remove(toAddressByChannel(channel), channel);
        }
    }

    protected boolean putChannelIfConnectSuccess(String address, Channel channel) {

        if (channel != null && channel.isOpen()) {
            CHANNEL_MAP.put(address, channel);
            return true;
        }
        return false;
    }

    protected String toAddressByChannel(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String host = address.getAddress().getHostAddress();
        int port = address.getPort();
        return host + ":" + port;
    }

    public Collection<Channel> getClientChannels() {
        return CHANNEL_MAP.values();
    }
}
