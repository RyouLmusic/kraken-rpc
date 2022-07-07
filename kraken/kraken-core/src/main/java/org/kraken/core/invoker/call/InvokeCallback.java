package org.kraken.core.invoker.call;

import org.kraken.core.remoting.protocol.Response;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/23 19:05
 */
public abstract class InvokeCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onFailure(Response response, Throwable exception);


    private static final ThreadLocal<InvokeCallback<?>> threadInvokeCallback = new ThreadLocal<>();

    public static void setInvokeCallback(InvokeCallback<?> invokeCallback) {
        threadInvokeCallback.set(invokeCallback);
    }

    public static InvokeCallback<?> getInvokeCallback() {
        InvokeCallback<?> invokeCallback = threadInvokeCallback.get();
        threadInvokeCallback.remove();
        return invokeCallback;
    }
}
