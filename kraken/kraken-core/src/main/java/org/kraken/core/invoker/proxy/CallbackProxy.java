package org.kraken.core.invoker.proxy;

import org.kraken.core.common.exception.InvokerException;
import org.kraken.core.invoker.call.FutureResponse;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;

/**
 * <p>
 *     HelloService service = reference.getObject(HelloService.class);
 *     InvokeCallback.setInvokeCallback(new InvokeCallback<User>() {
 *         @Override
 *         public void onSuccess(User result) {
 *             System.out.println(result);
 *
 *             System.out.println("ccc");
 *         }
 *
 *         @Override
 *         public void onFailure(Response response, Throwable exception) {
 *             System.out.println(".....");
 *         }
 *     });
 *     service.getUser();
 * </p>
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/1 20:40
 */
public class CallbackProxy extends AbstractProxy {


    public CallbackProxy(Class<? extends AbstractClient> clientClass, ReferenceBean reference) throws Throwable {
        super(clientClass, reference);
    }

    @Override
    protected Object doIntercept(ProtocolMessage message) throws Throwable {
        ProtocolHeader header = message.getHeader();
        FutureResponse futureResponse = new FutureResponse(invokerFactory, header.getSeq(), null);

        InvokeCallback invokeCallback = InvokeCallback.getInvokeCallback();
        if (invokeCallback == null) {
            throw new InvokerException("没有实现 callback function");
        }
        futureResponse.setInvokeCallback(invokeCallback);
        // 设置调用获取消息的时候的
        invokerFactory.setFutureResponse(String.valueOf(header.getSeq()), futureResponse);

        // 发送消息
        client.sent(message, true);

        return null;
    }
}
