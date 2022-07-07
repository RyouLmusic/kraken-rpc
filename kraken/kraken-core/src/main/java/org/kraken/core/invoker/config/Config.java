package org.kraken.core.invoker.config;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/13 0:09
 */
public interface Config {

    int getActive();
    long getTimeout();
    String getVersion();

}
