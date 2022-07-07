package com.kraken.filter;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.fiter.Filter;
import org.kraken.core.fiter.chain.Result;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.spring.annotation.KrakenFilter;
import org.springframework.stereotype.Component;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/4/28 20:31
 */
//@KrakenFilter
public class BFilter extends Filter {
    @Override
    public Result invoke(URL url, Request request) throws AppException {

        System.out.println("B --------------- B");

        return getNext().invoke(url, request);
    }
}
