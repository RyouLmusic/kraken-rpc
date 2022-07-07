package org.kraken.core.fiter;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.fiter.chain.Result;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/12 0:05
 */
public class ChainEndFilter extends Filter {

    /**
     * 在此处返回结果
     * @param url
     * @param request
     * @return
     * @throws AppException
     */
    @Override
    public Result invoke(URL url, Request request) throws AppException {
        return null;
    }
}
