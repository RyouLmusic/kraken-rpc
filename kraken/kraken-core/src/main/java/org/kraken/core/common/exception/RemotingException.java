package org.kraken.core.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/12 15:14
 */
@Slf4j
public class RemotingException extends AppException {

    public RemotingException() {
        super();
        log.error("远程连接出现异常...");
    }

    public RemotingException(String message) {
        super(message);
        log.error("远程连接出现异常：{}", message);
    }


    public RemotingException(Throwable cause) {
        super(cause);
        log.error("远程连接出现异常...");
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
        log.error("远程连接出现异常：{}", message);
    }
}
