package org.kraken.core.fiter;


import org.kraken.core.common.bean.RpcStatus;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.fiter.chain.Result;

import static org.kraken.core.common.utils.ConfigUtils.makeServiceKey;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/8 13:16
 *
 * 只能用于服务调用者端(消费者)
 * 实现限流算法  url.getActive() 为使用窗口大小，限流的强度
 */
public class ActiveLimitFilter extends Filter implements FilterListener {

    @Override
    public Result invoke(URL url, Request request) throws AppException {

//        String methodKey = makeMethodConfigKey(request.getMethodName(), request.getParamTypes());
        String methodKey = makeServiceKey(request.getInterfaceName(), request.getMethodName(), request.getParamTypes());

//        System.out.println("url.getUri() : " + methodKey);
        final RpcStatus rpcStatus = RpcStatus.getStatus(url, methodKey);
        // 判断是不是超过并发限制
        // RpcStatus 根据 URL 和调用方法名获取对应方法的RPC状态对象
        // RpcStatus.tryAcquire 返回false ：则让当前线程挂起，之后会在 timeout 时间后被唤醒，并抛出 RpcException 异常。

        /*System.out.println(url.getActive());
        System.out.println("rpcStatus.getActive() : " + rpcStatus.getActive());*/
        if (!RpcStatus.tryAcquire(url, methodKey)) {
            long timeout = url.getTimeout();
            long start = System.currentTimeMillis();
            synchronized (rpcStatus) {
                // 进行自选排队，等待处理
                while (!RpcStatus.tryAcquire(url, methodKey)) {
                    try {
//                        System.out.println("rpcStatus.getActive() : " + rpcStatus.getActive());
                        rpcStatus.wait(timeout);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    // 运行时间
                    long elapsed = System.currentTimeMillis() - start;
                    // 超时，抛出异常
                    if (timeout <= elapsed) {
                        throw new AppException("Waiting concurrent invoke timeout in client-side for service:  " +
                                request.getInterfaceName() + ", method: " + request.getMethodName()  +
                                        ", elapsed: " + elapsed + ", timeout: " + timeout + ". concurrent invokes: " +
                                        rpcStatus.getActive() + ". max concurrent invoke limit: " + url.getActive());
                    }
                }



            }

        }


        return getNext().invoke(url, request);
    }

    @Override
    public void onSuccess(Result appResponse, URL url, Request request) {
        String methodKey = makeServiceKey(request.getInterfaceName(), request.getMethodName(), request.getParamTypes());
        // 方法唤醒。
        RpcStatus.release(url, methodKey);
        RpcStatus status = RpcStatus.getStatus(url, methodKey);
        notifyFinish(status);
    }

    @Override
    public void onError(Throwable t, URL url, Request request) {
    }


    /**
     * 进行唤醒其他被阻塞的线程
     * @param rpcStatus
     */
    private void notifyFinish(final RpcStatus rpcStatus) {
        synchronized (rpcStatus) {
            rpcStatus.notifyAll();
        }
    }
}
