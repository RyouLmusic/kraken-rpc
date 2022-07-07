package org.kraken.core.fiter;

import org.kraken.core.common.bean.URL;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.fiter.chain.Result;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/11 22:16
 */
public interface FilterListener {
    void onSuccess(Result appResponse, URL url, Request request);
    void onError(Throwable t, URL url, Request request);
}
