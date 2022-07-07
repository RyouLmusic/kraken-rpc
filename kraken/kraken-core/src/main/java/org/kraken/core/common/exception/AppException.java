package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/21 9:40
 *
 * 全局异常
 */
@Slf4j
public class AppException extends RuntimeException {
    public AppException() {
        super();
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(Throwable cause) {
        super(cause);
    }
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

}
