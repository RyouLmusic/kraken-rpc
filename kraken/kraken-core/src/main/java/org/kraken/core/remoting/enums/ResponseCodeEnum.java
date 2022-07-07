package org.kraken.core.remoting.enums;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/7 11:31
 */
public enum ResponseCodeEnum {


    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call is fail");
    private Integer code;
    private String message;
    ResponseCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
