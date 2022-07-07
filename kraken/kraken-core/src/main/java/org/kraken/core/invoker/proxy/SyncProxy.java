package org.kraken.core.invoker.proxy;

import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;
import org.kraken.core.remoting.protocol.Response;
import org.kraken.core.invoker.call.FutureResponse;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/21 20:51
 *
 * 同步方式的 实现
 */
public class SyncProxy extends AbstractProxy {


    public SyncProxy(Class<? extends AbstractClient> clientClass, ReferenceBean reference) throws Throwable {
        super(clientClass, reference);
    }

    @Override
    protected Object doIntercept(ProtocolMessage message) {

        ProtocolHeader header = message.getHeader();

        FutureResponse futureResponse = new FutureResponse(invokerFactory, header.getSeq(), null);
        String seq = String.valueOf(header.getSeq());

        invokerFactory.setFutureResponse(seq, futureResponse);
        try {
            // 发送消息
            client.sent(message, true);
            // TODO
            Response response = futureResponse.get(/*timeout*/1000, TimeUnit.SECONDS);
            if (!response.isSuccess()) {
                throw new RemotingException(response.getMessage());
            }
            return response.getData();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            futureResponse.removeInvokerFuture();
        }
        return null;
    }



}
