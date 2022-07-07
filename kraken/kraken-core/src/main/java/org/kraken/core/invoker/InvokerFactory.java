package org.kraken.core.invoker;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.common.exception.InvokerException;
import org.kraken.core.fiter.Filter;
import org.kraken.core.remoting.enums.ResponseCodeEnum;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.remoting.protocol.Response;
import org.kraken.core.fiter.ActiveLimitFilter;
import org.kraken.core.fiter.ChainEndFilter;
import org.kraken.core.fiter.GenericFilter;
import org.kraken.core.fiter.chain.FilterChainBuilder;
import org.kraken.core.invoker.call.FutureResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * invoker factory, init service-registry
 */
@Slf4j
public class InvokerFactory {

    /*---------------------- default instance ----------------------*/
    // TODO 线程安全
    private static final InvokerFactory instance = new InvokerFactory();
    public static InvokerFactory getInstance() {
        return instance;
    }

    private InvokerFactory() {
        initFilter();
    }

    public void  stop() throws Exception {

        // stop callback
        if (stopCallbackList.size() > 0) {
            for (Runnable callback: stopCallbackList) {
                try {
                    callback.run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        // stop CallbackThreadPool
        stopCallbackThreadPool();
    }




    // ---------------------- service registry ----------------------

    private List<Runnable> stopCallbackList = new ArrayList<>();

    public void addStopCallBack(Runnable callback){
        stopCallbackList.add(callback);
    }


    // ---------------------- future-response pool ----------------------

    // XxlRpcFutureResponseFactory
    // 存储 <requestID ,FutureResponse>
    private final ConcurrentMap<String, FutureResponse> futureResponsePool = new ConcurrentHashMap<>();
    public void setFutureResponse(String seq, FutureResponse futureResponse){
        futureResponsePool.put(seq, futureResponse);
    }
    public void removeInvokerFuture(String seq){
        futureResponsePool.remove(seq);
    }

    /**
     * clientHandle中调用此方法(接收到response之后)，激活client线程
     * @param seq 序列号：
     * @param response  Response
     */
    public void notifyInvokerFuture(Long seq, Response response){

        // get
        final FutureResponse futureResponse = futureResponsePool.get(String.valueOf(seq));
        if (futureResponse == null) {
            return;
        }

        // 判断是否有设置回调方法
        if (futureResponse.getInvokeCallback() != null) {

            // 执行回调方法
            try {
                executeResponseCallback(() -> {
                    if (Objects.equals(response.getCode(), ResponseCodeEnum.FAIL.getCode())) {
                        futureResponse.getInvokeCallback().onFailure(Response.fail(ResponseCodeEnum.FAIL), new InvokerException("callback fail"));
                    } else {
                        futureResponse.getInvokeCallback().onSuccess(response.getData());
                    }
                });
            }catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {

            // 其他方式进行唤醒 client线程
            futureResponse.setResponse(response);
        }

        // do remove
        futureResponsePool.remove(String.valueOf(seq));

    }


    /* ---------------------- 执行回调方法的线程池 ---------------------- */

    private volatile ThreadPoolExecutor responseCallbackThreadPool = null;
    public void executeResponseCallback(Runnable runnable){
        // TODO
        if (responseCallbackThreadPool == null) {
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(
                            10,
                            100,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(1000),
                            r -> new Thread(r, "xxl-rpc, XxlRpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode()),
                            (r, executor) -> {
                                throw new AppException("Invoke Callback Thread pool is EXHAUSTED!");
                            });		// default maxThreads 300, minThreads 60
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }



    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

    /*--------------------------FilterChain-------------------------------*/
    FilterChainBuilder.Builder builder;

    private void initFilter() {
        builder = new FilterChainBuilder.Builder();
        builder.addFilter(new ActiveLimitFilter())
               .addFilter(new GenericFilter());
    }

    public void addFilter(Filter filter) {
        builder.addLastFilter(filter);
    }
    public void invoke(URL url, Request request) {

        FilterChainBuilder.invoke(url, request, builder);
    }

}
