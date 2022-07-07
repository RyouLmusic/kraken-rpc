package org.kraken.core.remoting.net.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kraken.core.common.bean.URL;
import org.kraken.core.invoker.InvokerFactory;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.net.AbstractEndpoint;
import org.kraken.core.remoting.protocol.Beat;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;
import org.kraken.core.remoting.protocol.Response;

import static org.kraken.core.common.bean.Constants.ENDPOINT_KEY;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/9 15:43
 *
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<ProtocolMessage> {
    /** 空闲次数 */
    private int idle_count = 1;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) throws Exception {
        // TODO ?? Request为什么会发送到这里来
        /*if (msg.getHeader().getType() == MessageTypeEnum.REQUEST.getValue()) {
            return;
        }*/
        AttributeKey<AbstractEndpoint> key = AttributeKey.valueOf(ENDPOINT_KEY);
        AbstractEndpoint endpoint = ctx.channel().attr(key).get();
        Channel channel = ctx.channel();
        // 处理心跳 在此判断
        byte type = msg.getHeader().getType();
        if (type == MessageTypeEnum.HEARTBEAT.getValue()) {
            heartBeatProcess(channel);
        } else if (type == MessageTypeEnum.RESPONSE.getValue()) {
            responseProcess(msg, endpoint);
        }

    }

    private void responseProcess(ProtocolMessage message, AbstractEndpoint endpoint) {
        ProtocolHeader header = message.getHeader();
        // 处理消息头
//        if (header.getSeq() )
        // 处理消息体
        Response response = (Response) message.getBody();
        // 唤醒操作
        InvokerFactory invokerFactory = InvokerFactory.getInstance();
        invokerFactory.notifyInvokerFuture(header.getSeq(), response);
    }


    /**
     * 处理心跳 消息
     */
    private void heartBeatProcess(Channel channel) {
        log.info("[heartBeat] connection:{} receive a heartbeat packet from server", channel.remoteAddress());
    }

    /**
     * 心跳请求处理，每4秒发送一次心跳请求;
     *
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (IdleState.WRITER_IDLE.equals(event.state())) { // 如果写通道处于空闲状态就发送心跳命令

                // 发送ping
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(Beat.ping());
                // 成功发送，就将idle_count计数清零
                if (channelFuture.isSuccess())
                    idle_count = 0;
                // 如果读空闲很久就需要进行重连
            }

        }

    }

    // TODO
    @SneakyThrows
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<AbstractEndpoint> key = AttributeKey.valueOf(ENDPOINT_KEY);
        AbstractClient client = (AbstractClient) ctx.channel().attr(key).get();
        client.disConnect();
        // TODO 将AbstractProxy.getProviderAddress()送到此处
//        int reconnect_times = 0;

        while (!client.isValidate() && idle_count <= 2) {
//            reconnect_times++;
            idle_count++;
            try {

                try {
                    Thread.sleep(3000);
                    client.connect();
                } catch (Throwable ignored) {
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // TODO 进行更新地址 重新设置新的address， client，然后重新连接
        URL url = client.selectClusterAddress();
        client.reconnect(url.getInetAddress());
        if (!client.isValidate()) {
            log.info("Ping failed to be sent for more than two times, and the channel was closed : {}", ctx.channel().remoteAddress());
            client.disConnect();
        } else {
            log.info("Succeeded in connecting to the new server : {}", ctx.channel().remoteAddress());
            idle_count = 0;
        }
        super.channelInactive(ctx);
    }
}
