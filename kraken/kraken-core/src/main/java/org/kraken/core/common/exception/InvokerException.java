package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/21 21:12
 */
@Slf4j
public class InvokerException extends AppException {

    public InvokerException() {
        super();
        log.error("[invoker操作出现异常...]");
    }

    public InvokerException(String message) {
        super(message);
        log.error("[invoker操作出现异常：{}]", message);
    }


    public InvokerException(Throwable cause) {
        super(cause);
        log.error("[invoker操作出现异常...]");
    }

    public InvokerException(String message, Throwable cause) {
        super(message, cause);
        log.error("[invoker操作出现异常：{}]", message);
    }
}
