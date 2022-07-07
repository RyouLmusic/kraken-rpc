package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/21 9:39
 *
 * 处理序列化异常
 */
@Slf4j
public class SerializeException extends AppException {

    public SerializeException() {
        super();
        log.error("序列化出现异常...");
    }

    public SerializeException(String message) {
        super(message);
        log.error("序列化出现异常：{}", message);
    }

    public static void main(String[] args) throws SerializeException {
        throw new SerializeException("255");
    }
}
