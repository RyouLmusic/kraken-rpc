package org.kraken.core.remoting.net;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.invoker.provider.ProviderFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/17 14:49
 */
@Slf4j
public abstract class AbstractServer extends AbstractEndpoint {

    protected final AtomicBoolean closed;

    /**
     * 服务器进行绑定的地址
     */
    private final InetSocketAddress bindAddress;

    private ProviderFactory providerFactory;

    public AbstractServer(URL url) {
        bindAddress = url.getInetAddress();
        closed = new AtomicBoolean(true);
    }


    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    /**
     * is bound.
     *
     * @return bound
     */
    protected abstract boolean isBound();

    /**
     * get channels.
     *
     * @return channels
     */
    protected abstract Collection<Channel> getChannels();

    /**
     * get channel.
     *
     * @param remoteAddress
     * @return channel
     */
    protected abstract Channel getChannel(InetSocketAddress remoteAddress);

    public void open() throws Throwable {

        // 如果已经开启，closed == false 不需要再次开启
        if (!closed.get()) {
            log.info("[kraken] netty server has started, not need to start again");
        }
        try {
            doOpen();
            closed.set(false);
        } catch (Throwable t) {
            throw new RemotingException(getBindAddress() + "Failed to bind " + getClass().getSimpleName()
                    + "on server , cause: " + t.getMessage(), t);
        }
    }
    public void close() throws Throwable {
        if (closed.get()) {
            log.info("[kraken] netty server had closed");
            return;
        }
        try {
            doClose();
        } catch (Throwable t) {
            throw new RemotingException(getBindAddress() + "Failed to close " + getClass().getSimpleName()
                    + "at server , cause: " + t.getMessage(), t);
        }
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }



    /*---------------------------- AbstractEndpoint --------------------------------*/

    /**
     * 给所有存活的channel发送消息
     * @param message message.
     * @param sent sent.
     * @throws RemotingException 抛出异常
     */
    @Override
    public void sent(Object message, boolean sent) throws RemotingException {
        getChannels()
                .parallelStream()
                .forEach(channel -> sent(message, sent, channel));
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void reconnect(InetSocketAddress address) throws RemotingException {

    }

    public ProviderFactory getProvider() {
        return providerFactory;
    }

    public void setProviderFactory(ProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }
}
