package org.kraken.core.fiter;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.fiter.chain.Result;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/8 13:16
 */
public interface BaseFilter {


    /**
     * Always call filter.invoke() in the implementation to hand over the request to the next filter node.
     */
    Result invoke(URL url, Request request) throws AppException;

}
