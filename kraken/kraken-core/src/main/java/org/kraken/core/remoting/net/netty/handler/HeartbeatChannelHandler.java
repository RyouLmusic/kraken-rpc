package org.kraken.core.remoting.net.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/9 16:19
 */
@Slf4j
public class HeartbeatChannelHandler extends ChannelDuplexHandler {

    public static final HeartbeatChannelHandler INSTANCE = new HeartbeatChannelHandler();

    private HeartbeatChannelHandler() {}

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            ctx.channel().attr(AttributeKey.valueOf("ssd")).set(System.currentTimeMillis());
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
//            ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
        ctx.fireChannelActive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.channel().attr(ChannelAttrKeys.lastReadTimeMillis).set(System.currentTimeMillis());
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
