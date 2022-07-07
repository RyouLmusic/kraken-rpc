package org.kraken.core.invoker.proxy;

import org.kraken.core.invoker.call.FutureResponse;
import org.kraken.core.invoker.call.InvokeFuture;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;

import java.util.concurrent.ExecutionException;


/**
 * <P>
 * FUTURE方式
 * 使用：
 *     <p>Service service = referenceBean.getObject(Service.class);</p>
 *     <p>service.service()</p>
 *     <p>FutureProxy future = FutureProxy.getFuture(User.class)</p>
 *     <p>User user = future.get();</p>
 *     <p>每次执行service.service()，都必须先执行对应的future，然后才能执行其他的service否则会出现转换异常</p>
 * </P>
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/1 16:19
 */
public class FutureProxy extends AbstractProxy {

    public FutureProxy(Class<? extends AbstractClient> clientClass, ReferenceBean reference) throws Throwable {
        super(clientClass, reference);
    }

    @Override
    protected Object doIntercept(ProtocolMessage message) throws ExecutionException, InterruptedException {
        ProtocolHeader header = message.getHeader();
        // 获取响应的类
        FutureResponse futureResponse = new FutureResponse(invokerFactory, header.getSeq(), null);
        // Future
        InvokeFuture invokeFuture = new InvokeFuture(futureResponse);
        InvokeFuture.setFuture(invokeFuture);
        //
        invokerFactory.setFutureResponse(String.valueOf(header.getSeq()), futureResponse);

        // 发送消息
        client.sent(message, true);
        return null;

    }

}
