package org.kraken.core.remoting.net;

import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.kraken.core.common.config.TLSConfig;
import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.common.utils.NetUtils;
import org.kraken.core.remoting.bean.NettyChannelContext;
import org.kraken.core.invoker.provider.ProviderFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/17 14:46
 */
public abstract class AbstractEndpoint extends NettyChannelContext implements Endpoint {

    protected SslContext sslContext;
    private static final String CLASSPATH = "classpath:";

    @Override
    public ChannelHandler getChannelHandler() {
        return null;
    }

    @Override
    public void reconnect(InetSocketAddress address) throws RemotingException {
    }

    public abstract ProviderFactory getProvider();
    /**
     * on message sent.
     *
     * @param message message.
     * @param sent weather before timeout completed.
     */
    public abstract void sent(Object message, boolean sent) throws RemotingException;
    /**
     * on message received.
     *
     * @param channel channel.
     * @param message message.
     */
    public abstract void received(Channel channel, Object message) throws RemotingException;


    public void sent(Object message, boolean sent, Channel channel) throws RemotingException {
        if (!channel.isOpen()) {
            throw new RemotingException("Failed to send message");
        }

        boolean success = true;
        int timeout = 0;
        try {
            ChannelFuture future = channel.writeAndFlush(message);
            if (sent) {
                // wait timeout ms TODO
                timeout = 1000;
                success = future.await(timeout);
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            removeChannelIfDisconnected(channel);
            throw new RemotingException("Failed to send message", e);
        }
        if (!success) {
            throw new RemotingException("Failed to send message to "
                    + NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress())
                    + "in timeout(" + timeout + "ms) limit");
        }
    }


    /* ----------------------------- TLS ---------------------------------*/

    /**
     * 开启TLS的前置操作，实例化sslContext对象
     * @param forClient 是否是客户端
     * @param tlsConfig 配置
     * @throws Exception Exception
     */
    protected void enableTLSEncryption(boolean forClient, TLSConfig tlsConfig) throws Exception {
        InputStream key = null;
        InputStream cer = null;
        InputStream trust = null;
        try {
            // 获取密钥
            key = parseInputStream(tlsConfig.getKeyPath());
            cer = parseInputStream(tlsConfig.getCertPath());
            SslContextBuilder sslContextBuilder;
            if (forClient) {
                sslContextBuilder = SslContextBuilder.forClient().keyManager(cer, key, tlsConfig.getKeyPwd());
            } else {
                sslContextBuilder = SslContextBuilder.forServer(cer, key, tlsConfig.getKeyPwd());
                sslContextBuilder.clientAuth(parseClientAuthMode(tlsConfig.getClientAuth()));
            }
            sslContextBuilder.sslProvider(sslProvider());

            if (tlsConfig.getTrustCertPath() == null || tlsConfig.getTrustCertPath().trim().isEmpty()) {
                sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else {
                trust = parseInputStream(tlsConfig.getTrustCertPath());
                sslContextBuilder.trustManager(trust);
            }
            sslContext = sslContextBuilder.build();
        } finally {
            if (cer != null) {
                cer.close();
            }
            if (key != null) {
                key.close();
            }
            if (trust != null) {
                trust.close();
            }
        }
    }

    private InputStream parseInputStream(String path) throws FileNotFoundException {
        if (path.startsWith(CLASSPATH)) {
            path = path.replaceFirst(CLASSPATH, "");
            return this.getClass().getClassLoader().getResourceAsStream(path);
        }
        return new FileInputStream(path);
    }

    protected ClientAuth parseClientAuthMode(String authMode) {
        if (authMode == null || authMode.trim().isEmpty()) {
            return ClientAuth.NONE;
        }
        for (ClientAuth clientAuth : ClientAuth.values()) {
            if (clientAuth.name().equals(authMode.toUpperCase())) {
                return clientAuth;
            }
        }
        return ClientAuth.NONE;
    }

    protected SslProvider sslProvider() {
        if (OpenSsl.isAvailable()) {
            return SslProvider.OPENSSL;
        } else {
            return SslProvider.JDK;
        }
    }


}
