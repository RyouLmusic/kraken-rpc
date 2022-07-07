package org.kraken.core.remoting.protocol;

import org.kraken.core.remoting.enums.ResponseCodeEnum;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/7 13:24
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Response extends MessageBody implements Serializable {

    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private Object data;

    public static Response success(Object data, Long requestId) {
        Response response = new Response();
        response.setCode(ResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(ResponseCodeEnum.SUCCESS.getMessage());
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static Response fail(ResponseCodeEnum responseCodeEnum) {
        Response response = new Response();
        response.setCode(responseCodeEnum.getCode());
        response.setMessage(responseCodeEnum.getMessage());
        return response;
    }

    public boolean isSuccess() {
        return Objects.equals(ResponseCodeEnum.SUCCESS.getCode(), code);
    }

}
