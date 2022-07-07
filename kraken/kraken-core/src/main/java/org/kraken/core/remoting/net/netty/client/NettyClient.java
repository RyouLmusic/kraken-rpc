package org.kraken.core.remoting.net.netty.client;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.config.NettyConfig;
import org.kraken.core.common.config.TLSConfig;
import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.common.utils.NetUtils;
import org.kraken.core.remoting.codec.MessageDecode;
import org.kraken.core.remoting.codec.MessageEncode;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.invoker.provider.ProviderFactory;
import org.kraken.core.remoting.net.netty.handler.ChannelHandle;
import org.kraken.core.remoting.net.netty.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/7 13:24
 */
@Slf4j
public class NettyClient extends AbstractClient {

    private EventLoopGroup workers;
    private DefaultEventExecutorGroup eventExecutors;

    private Bootstrap bootstrap;

    private final NettyConfig nettyConfig = ConfigUtils.getNettyConfig();

    private final TLSConfig tlsConfig = ConfigUtils.getTLSConfig();

    /**
     * 每次建立的连接的通道
     */
    protected volatile Channel channel;
    /**
     * 连接是否关闭
     * 默认是关闭
     */
    private final AtomicBoolean isClose = new AtomicBoolean(true);

    /**
     *
     */
    public NettyClient(URL url) {
        super(url);
    }


    /*---------------------------- AbstractClient ------------------------------------------*/

    @Override
    protected void doOpen() throws Throwable {
        if (useEpoll()) {
            workers = new EpollEventLoopGroup(Constants.DEFAULT_THREADS);
        } else {
            workers = new NioEventLoopGroup(Constants.DEFAULT_THREADS);
        }
        bootstrap = new Bootstrap();

        eventExecutors = new DefaultEventExecutorGroup(Constants.DEFAULT_THREADS);

        bootstrap.group(workers)
                // 长连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                // TCP 无延迟
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(useEpoll() ? EpollSocketChannel.class : NioSocketChannel.class)
                // 设置timeout
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,1000);

        // TODO TLS加密
        if (tlsConfig.getUseTLS() != null && tlsConfig.getUseTLS()) {
            enableTLSEncryption(true, tlsConfig);
        }


        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline
                        .addLast(eventExecutors, "decoder", new MessageDecode())
                        .addLast(eventExecutors, "encoder", new MessageEncode())
                        .addLast(eventExecutors, new ChannelHandle(NettyClient.this))
                        .addLast(eventExecutors, "heartbeat_ping", new IdleStateHandler(0, nettyConfig.getHeartbeatIntervalTime(), 0, TimeUnit.SECONDS))
                        .addLast(eventExecutors, new ClientHandler());
                //tls加密
                if (sslContext != null) {
                    pipeline.addLast(eventExecutors, "sslHandler", sslContext.newHandler(channel.alloc()));
                }
            }
        });


    }

    /**
     * 而且还关闭 bootstrap
     * @throws Throwable
     */
    @Override
    protected void doClose() throws Throwable {

        doDisConnect();
        if (bootstrap != null) {
            bootstrap.clone();
        }
        if (workers != null && !workers.isShutdown()) {
            workers.shutdownGracefully();
        }
        if (eventExecutors != null && !eventExecutors.isShutdown()) {
            eventExecutors.shutdownGracefully();
        }
    }
    @Override
    public void doConnect()throws Throwable {
        doConnect(getConnectAddress());
    }
    @Override
    protected void doConnect(InetSocketAddress address) throws Throwable {
        // TODO 时间记录
        long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(address);
        try {
            /**
             * Waits for this future to be completed within the specified time limit without interruption.
             */
            boolean ret = future.awaitUninterruptibly(nettyConfig.getConnectTimeoutMillis());

            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    // 关闭旧通道
                    // copy reference
                    Channel oldChannel = this.channel;
                    if (oldChannel != null) {
                        try {
                            log.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                            oldChannel.close();
                        } finally {
                            // 将管理里面的 channel也删除
                            removeChannelIfDisconnected(oldChannel);
                        }
                    }
                    // 添加到
                    boolean success = putChannelIfConnectSuccess(getAddress(), newChannel);
                    if (success)
                        isClose.set(false);
                } finally {
                    // 如果已经设置为关闭的话，将newChannel也关闭
                    if (isClosed()) {
                        try {
                            log.info("Close new netty channel " + newChannel + ", because the client closed.");
                            newChannel.close();
                        } finally {
                            channel = null;
                            removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        channel = newChannel;
                    }
                }
            } else if (future.cause() != null) {
                throw new RemotingException("client(url: ) failed to connect to server ", future.cause());
            } else {
                throw new RemotingException("client(url: ");
            }
        } finally {

            if (channel != null && channel.isOpen()) {
                // 如果成功开启连接 将是否关闭的 标志设置为 false
                isClose.set(false);
            }
        }
    }

    private String getAddress() {
        return NetUtils.toAddressString(getConnectAddress());
    }

    /**
     * 跟Server的连接 是否关闭
     * @return boolean
     */
    private boolean isClosed() {
        return isClose.get();
    }
    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }
    @Override
    protected void doDisConnect() throws Throwable {
        if (channel != null && channel.isOpen() && !isClosed()) {
            channel.closeFuture();
            channel.close();
            removeChannelIfDisconnected(channel);
        }
        if (!channel.isActive()) {
            isClose.set(true);
        }
    }

    // 实现 AbstractClient 方法
    @Override
    public Channel getChannel() {
        return channel;
    }

    /**
     * 是否已经连接上服务器
     * @return boolean
     */
    @Override
    public boolean isConnect() {
        return !isClose.get();
    }

    /**
     * 判断是否可以使用epoll方法
     * @return true 可以使用
     */
    private boolean useEpoll() {
        return Epoll.isAvailable() && System.getProperty("os.name").toLowerCase().contains("linux");
    }

    /**
     * 不需要用到，是server端用到的
     * @return null
     */
    @Override
    public ProviderFactory getProvider() {
        return null;
    }
}


