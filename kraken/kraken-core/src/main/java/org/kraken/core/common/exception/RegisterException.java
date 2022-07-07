package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 16:08
 */
@Slf4j
public class RegisterException extends AppException {
    public RegisterException() {
        super();
        log.error("ZK操作出现异常...");
    }

    public RegisterException(String message) {
        super(message);
        log.error("ZK操作出现异常：{}", message);
    }


    public RegisterException(Throwable cause) {
        super(cause);
        log.error("ZK操作出现异常...");
    }

    public RegisterException(String message, Throwable cause) {
        super(message, cause);
        log.error("ZK操作出现异常：{}", message);
    }
}
