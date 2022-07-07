package org.kraken.core.invoker.proxy;

import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.protocol.ProtocolMessage;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/1 21:45
 */
public class OnewayProxy extends AbstractProxy {


    public OnewayProxy(Class<? extends AbstractClient> clientClass, ReferenceBean reference) throws Throwable {
        super(clientClass, reference);
    }

    @Override
    protected Object doIntercept(ProtocolMessage message){
        client.sent(message, true);
        return null;
    }
}
