package org.kraken.core.remoting.net.netty.server;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.config.TLSConfig;
import org.kraken.core.common.utils.CollectionUtils;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.common.utils.NetUtils;
import org.kraken.core.remoting.codec.MessageDecode;
import org.kraken.core.remoting.codec.MessageEncode;
import org.kraken.core.remoting.net.AbstractServer;
import org.kraken.core.remoting.net.netty.handler.ChannelHandle;
import org.kraken.core.remoting.net.netty.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/7 20:02
 */
@Slf4j
public class NettyServer extends AbstractServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup eventExecutors;

    private ServerBootstrap bootstrap;

    private final TLSConfig tlsConfig = ConfigUtils.getTLSConfig();
    /**
     * the cache for alive worker channel.
     * <ip:port, channel>
     */
    private Map<String, Channel> channels;
    /**
     * the boss channel that receive connections and dispatch these to worker channel.
     */
    private Channel channel;

    public NettyServer(URL url) {
        super(url);
    }


    public static void main(String[] args) throws Throwable {

        new NettyServer(new URL("localhost", 8002)).doOpen();
    }



    @Override
    protected void doOpen() throws Throwable{

        if (useEpoll()) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(Constants.DEFAULT_THREADS);
        } else {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(Constants.DEFAULT_THREADS);
        }


        eventExecutors = new DefaultEventExecutorGroup(Constants.DEFAULT_THREADS);
        // TODO TLS加密
        if (tlsConfig.getUseTLS() != null && tlsConfig.getUseTLS()) {
            enableTLSEncryption(false, tlsConfig);
        }
        channels = new ConcurrentHashMap<>();

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 定义了待接受连接的最大队列长度，如果一个连接到来后发现队列是满的，发起连接请求的 client 将会受到一个 ECONNREFUSED 类型的错误。另外，如果底层协议支持重传，这个请求将被忽略，重传将有可能成功。
                .option(ChannelOption.SO_BACKLOG, 128)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline
                                .addLast(eventExecutors, "decoder", new MessageDecode())
                                .addLast(eventExecutors, "encoder", new MessageEncode())
                                .addLast(eventExecutors, new ChannelHandle(NettyServer.this))
                                // 当在指定的时间间隔内没有从 Channel 读取到数据时，会触发一个 READER_IDLE 的 IdleStateEvent 事件。
                                .addLast(eventExecutors, "heartbeat_pong", new IdleStateHandler(5,0,0, TimeUnit.SECONDS))
                                .addLast(eventExecutors, new ServerHandler());
                        //tls加密
                        if (sslContext != null) {
                            pipeline.addLast(eventExecutors, "sslHandler", sslContext.newHandler(channel.alloc()));
                        }

                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(getBindAddress());
        log.info("Server start listen at " + getBindAddress().getPort());
        // Waits for this future until it is done, and rethrows the cause of the failure if this future failed.
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();

    }

    @Override
    protected void doClose() {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            Collection<Channel> channels = getChannels();
            if (CollectionUtils.isNotEmpty(channels)) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                // TODO  serverShutdownTimeoutMills
                long timeout = 10000;
                long quietPeriod = Math.min(2000L, timeout);
                // 将 bossGroup workerGroup 关闭
                Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully(quietPeriod, timeout, MILLISECONDS);
                bossGroupShutdownFuture.syncUninterruptibly();
                workerGroupShutdownFuture.syncUninterruptibly();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    protected Collection<Channel> getChannels() {
        Collection<Channel> chs = new ArrayList<>(channels.size());
        // pick channels from NettyServerHandler ( needless to check connectivity )
        chs.addAll(channels.values());
        return chs;
    }

    @Override
    protected Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }

    @Override
    protected boolean isBound() {
        return false;
    }

    /**
     * 判断是否可以使用epoll方法
     * @return true 可以使用
     */
    private boolean useEpoll() {
        return Epoll.isAvailable() && System.getProperty("os.name").toLowerCase().contains("linux");
    }

    // TODO add channels
}
