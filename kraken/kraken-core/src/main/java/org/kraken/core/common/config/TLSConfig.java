package org.kraken.core.common.config;

import lombok.Data;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/7 15:02
 *
 * TLS协议相关配置
 */
@Data
public class TLSConfig {

    private Boolean useTLS; // 是否开启tls加密
    private String keyPath; //私钥
    private String keyPwd; //密码
    private String certPath; //证书路径
    private String trustCertPath; //受信任ca证书路径
    private String clientAuth; //模式
}
