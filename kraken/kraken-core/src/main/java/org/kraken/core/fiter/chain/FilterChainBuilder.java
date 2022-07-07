package org.kraken.core.fiter.chain;


import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.fiter.ChainEndFilter;
import org.kraken.core.fiter.Filter;
import org.kraken.core.fiter.FilterListener;
import org.kraken.core.remoting.protocol.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/8 14:04
 * 过滤责任链建立器
 */
public class FilterChainBuilder {

    public static class Builder {
        private Filter head;
        private Filter tail;

        private Filter end = new ChainEndFilter();

        public Builder addFilter(Filter filter) {
            if (this.head == null) {
                this.head = this.tail = filter;
                this.tail.setNext(end);
                return this;
            }
            this.tail.setNext(filter);
            this.tail = filter;
            this.tail.setNext(end);
            return this;
        }

        public void addLastFilter(Filter filter) {
            this.tail.setNext(filter);
            this.tail = filter;
            this.tail.setNext(end);
        }

        public Filter build() {
            return this.head;
        }

    }


    public static void invoke(URL url, Request request, Builder builder) {
        Filter filter = builder.build();
        // 存放带有监听的类，在最后进行执行onSuccess,或者onErr
        List<FilterListener> listeners = new ArrayList<>();

        Throwable t = null;
        // 执行链条
        Result response = null;
        try {
            response = filter.invoke(url, request);

        } catch (AppException e) {
            t = e;
        }
        while (filter != null) {
            // 添加到集合中
            if (filter instanceof FilterListener) {
                listeners.add((FilterListener) filter);
            }
            filter = filter.getNext();
        }
        // 执行监听方法
        if (t == null) {
            Result finalResponse = response;
            listeners.forEach((filterListener -> filterListener.onSuccess(finalResponse, url, request)));
        } else {
            Throwable finalT = t;
            listeners.forEach((filterListener -> filterListener.onError(finalT, url, request)));
        }

    }

}
