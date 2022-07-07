package org.kraken.core.invoker.call;

import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.remoting.protocol.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/23 16:04
 *
 * Future
 */
public class InvokeFuture implements Future {
    /**
     * 执行阻塞获取操作的 类
     */
    private FutureResponse futureResponse;
    /**
     * 每个请求的线程有自己独自的 InvokeFuture
     * 然后这个请求通过自己的Future去获取请求结果
     */
    private static ThreadLocal<InvokeFuture> threadInvokeFuturePool = new ThreadLocal<>();

    public InvokeFuture(FutureResponse futureResponse) {
        this.futureResponse = futureResponse;
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
        return false;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new RemotingException("Future获取响应失败");
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        try {
            // 阻塞等待获取响应
            Response response = futureResponse.get(timeout, unit);

            if (!response.isSuccess()) {
                throw new RemotingException("Future获取响应失败");
            }
            return response.getData();
        } finally {
            // 将 futureResponse 里面的 invokerFactory 删除，防止内存溢出
            futureResponse.removeInvokerFuture();
        }

    }

    /**
     * 获取 Future，然后通过 Future 去获取自己的Class
     * @param clazz 类型
     * @param <T> 返回的类型：service的方法里面最终返回的结果
     * @return clazz
     */
    public static <T> Future<T> getFuture(Class<T> clazz) {
        Future<T> future = (Future<T>) threadInvokeFuturePool.get();
        threadInvokeFuturePool.remove();
        return future;
    }

    /**
     * 设置 Future
     * @param future InvokeFuture
     */
    public static void setFuture(InvokeFuture future) {
        threadInvokeFuturePool.set(future);
    }

    /**
     * remove future
     */
    public static void removeFuture() {
        threadInvokeFuturePool.remove();
    }

}
