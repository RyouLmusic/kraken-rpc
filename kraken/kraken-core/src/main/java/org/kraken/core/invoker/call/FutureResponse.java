package org.kraken.core.invoker.call;

import org.kraken.core.common.exception.AppException;
import org.kraken.core.invoker.InvokerFactory;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.remoting.protocol.Response;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/23 13:51
 *
 * 接收客户端发送来的消息
 */
public class FutureResponse implements Future<Response> {
    /**
     * 执行工厂
     */
    private final InvokerFactory invokerFactory;

    // net data
    private Request request;
    private Response response;

    private final Long seq;
    //

    // lock
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicBoolean done = new AtomicBoolean(false);

    // 回调方法
    private InvokeCallback invokeCallback;


    public FutureResponse(InvokerFactory invokerFactory, Long seq, InvokeCallback invokeCallback) {
        this.invokerFactory = invokerFactory;
        this.seq = seq;
        this.invokeCallback = invokeCallback;
    }
    public void setResponse(Response response) {
        this.response = response;
        latch.countDown();
        done.set(true);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done.get();
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException {
        return get(-1, TimeUnit.MILLISECONDS);
    }
    @Override
    public Response get(long timeout, TimeUnit unit) {
        // 成功 的话 应该为 true;
        boolean await = true;
        // response 还未实例化
        if (!done.get()) {

            try {
                if (timeout < 0) {
                    latch.await();
                } else {
                    await = false;
                    long timeoutMillis = (TimeUnit.MILLISECONDS == unit)
                            ? timeout : TimeUnit.MILLISECONDS.convert(timeout, unit);
                    await = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO
        if (!done.get() || !await || response == null) {
            throw new AppException("request timeout at:"+ System.currentTimeMillis() +", request:" + request.toString());
        }
        return response;
    }

    /*---------------------------- callback方式 ----------------------------------*/
    public void setInvokeCallback(InvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public void removeInvokerFuture(){
        this.invokerFactory.removeInvokerFuture(String.valueOf(seq));
    }

}
