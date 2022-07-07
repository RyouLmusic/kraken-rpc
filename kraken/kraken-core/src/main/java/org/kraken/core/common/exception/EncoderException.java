package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 21:46
 */
@Slf4j
public class EncoderException extends AppException {
    public EncoderException() {
        super();
        log.error("编码操作出现异常...");
    }

    public EncoderException(String message) {
        super(message);
        log.error("编码操作出现异常：{}", message);
    }


    public EncoderException(Throwable cause) {
        super(cause);
        log.error("编码操作出现异常...");
    }

    public EncoderException(String message, Throwable cause) {
        super(message, cause);
        log.error("编码操作出现异常：{}", message);
    }
}
