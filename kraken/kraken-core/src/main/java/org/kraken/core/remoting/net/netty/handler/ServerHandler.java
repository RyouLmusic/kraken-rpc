package org.kraken.core.remoting.net.netty.handler;

import org.kraken.core.fiter.ActiveLimitFilter;
import org.kraken.core.fiter.chain.FilterChainBuilder;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import org.kraken.core.remoting.enums.ResponseCodeEnum;
import org.kraken.core.remoting.net.AbstractEndpoint;
import org.kraken.core.remoting.net.duplicate.DefaultDuplicateMarker;
import org.kraken.core.remoting.net.duplicate.DuplicatedMarker;
import org.kraken.core.remoting.protocol.*;
import org.kraken.core.invoker.provider.ProviderFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import static org.kraken.core.common.bean.Constants.ENDPOINT_KEY;


/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/14 13:56
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler {
    /** 空闲次数 */
    private int idle_count = 1;
    private final DuplicatedMarker duplicatedMarker;

    public ServerHandler() {
        duplicatedMarker = new DefaultDuplicateMarker();
        duplicatedMarker.initMarkerConfig(10000, Integer.MAX_VALUE);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
        ProtocolMessage msg = (ProtocolMessage) obj;
        Channel channel = ctx.channel();
        AttributeKey<AbstractEndpoint> key = AttributeKey.valueOf(ENDPOINT_KEY);
        AbstractEndpoint endpoint = channel.attr(key).get();

        // 处理心跳 在此判断
        byte type = msg.getHeader().getType();
        if (type == MessageTypeEnum.HEARTBEAT.getValue()) {
            heartBeatProcess(msg, endpoint, channel);
        } else if (type == MessageTypeEnum.REQUEST.getValue()) {
            requestProcess(msg, endpoint, channel);
        }

    }

    /*--------------------------FilterChain-------------------------------*/
    FilterChainBuilder.Builder builder;

    /**
     * 服务端接收到请求之后的操作
     * TODO 请求去重  request 的 seq  集群部署模式需使用redis实现去重
     * @param obj 请求消息
     * @param endpoint attar
     * @param channel 通道
     */
    private void requestProcess(Object obj, AbstractEndpoint endpoint, Channel channel) {
        ProtocolMessage message = (ProtocolMessage) obj;

        // ---去重
        long seq = message.getHeader().getSeq();

        if (duplicatedMarker.mark(seq)) {
            log.warn("Received duplicate request seq：[{}], ignore it", seq);
            // 直接返回，无需处理
            /*Response response = Response.duplicateRequest(seq);
            context.getConnection().send(response);*/
            return;
        }

        ProviderFactory providerFactory = endpoint.getProvider();
        // ---- 通过请求获取响应体
        Response responseBody = providerFactory.invokeService((Request) message.getBody());
        responseBody.setCode(ResponseCodeEnum.SUCCESS.getCode());
        responseBody.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
        //---------- 构建response
        ProtocolMessage response = new ProtocolMessage();
        // -----构建响应头
        ProtocolHeader header = ProtocolHeader.builtByReqHeader(message.getHeader());
        response.setHeader(header);
        response.setBody(responseBody);
        // 进行响应
        endpoint.sent(response, true, channel);
    }


    /**
     * 处理心跳 消息
     * @param message 消息
     * @param endpoint 连接点
     */
    private void heartBeatProcess(ProtocolMessage message, AbstractEndpoint endpoint, Channel channel) {
        log.info("[heartBeat]connection:{} receive a heartbeat packet from client", channel.remoteAddress());
        endpoint.sent(Beat.pong(), true, channel);
    }

    /**
     * 超时处理，如果5秒没有收到客户端的心跳，就触发; 如果超过两次，则直接关闭;
     *
     * TODO 没有触发
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.READER_IDLE.equals(event.state())) { // 如果读通道处于空闲状态，说明没有接收到心跳命令
                log.info("No message from the client is received after waiting for 5 seconds");
                if (idle_count > 3) {
                    Channel channel = ctx.channel();
                    log.info("there are no client requests for more than two times, close the channel: {}", channel.remoteAddress());
                    ctx.channel().close();
                }

                idle_count++;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
