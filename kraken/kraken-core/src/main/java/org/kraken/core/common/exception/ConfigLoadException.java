package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/26 21:27
 *
 *
 */
@Slf4j
public class ConfigLoadException extends AppException {

    public ConfigLoadException() {
        super();
        log.error("配置信息bean出现异常...");
    }

    public ConfigLoadException(String message) {
        super(message);
        log.error("配置信息bean出现异常：{}", message);
    }
}
