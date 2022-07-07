package org.kraken.core.remoting.net.netty.handler;


import org.kraken.core.common.bean.Constants;
import org.kraken.core.remoting.net.AbstractEndpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/28 16:05
 */
public class ChannelHandle extends ChannelInboundHandlerAdapter {
    private final AbstractEndpoint endpoint;
    public ChannelHandle(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 将 endpoint 塞入 attr
     * @param ctx 上下文
     * @param msg 消息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AttributeKey<AbstractEndpoint> key = AttributeKey.valueOf(Constants.ENDPOINT_KEY);
        ctx.channel().attr(key).set(endpoint);
        super.channelRead(ctx, msg);
    }

    /**
     * 心跳
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {

            /*int maxIdleTimeout = ctx.channel().attr(ChannelAttrKeys.maxIdleTimeout).get();
            long expireTime = System.currentTimeMillis() - ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).get();
            if (expireTime >= maxIdleTimeout * 1000) {
                log.warn("readIdleTimeout exceed maxIdleTimeout, real timeout {}, this channel[{}] will be closed",
                        expireTime, ctx.channel().toString());
                ChannelUtil.closeChannel(ctx.channel());
            } else if (ChannelUtil.clientSide(ctx)) {
                // send heart beat to remote peer
                ctx.writeAndFlush(RequestProtocol.newHeartbeat());
            }*/
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
