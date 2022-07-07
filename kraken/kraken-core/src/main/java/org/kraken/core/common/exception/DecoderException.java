package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/13 12:01
 */
@Slf4j
public class DecoderException extends AppException {

    public DecoderException() {
        super();
        log.error("解码操作出现异常...");
    }

    public DecoderException(String message) {
        super(message);
        log.error("解码操作出现异常：{}", message);
    }


    public DecoderException(Throwable cause) {
        super(cause);
        log.error("解码操作出现异常...");
    }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
        log.error("解码操作出现异常：{}", message);
    }
}
