package org.kraken.core.remoting.protocol;

import lombok.*;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/17 14:45
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class Request extends MessageBody implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 参数
     */
    private Object[] parameters;
    /**
     * 参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 接口版本号：解决接口的多种实现
     */
    private String version;
    /**
     * 指定的组
     */
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
