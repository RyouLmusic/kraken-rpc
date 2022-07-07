package org.kraken.spring.starter.properties;

import org.kraken.core.compress.Compress;
import org.kraken.core.invoker.proxy.*;
import org.kraken.core.loadbalance.LoadBalance;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static org.kraken.core.compress.Compress.Type.*;
import static org.kraken.core.loadbalance.LoadBalance.Type.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 16:57
 */
@Component
@ConfigurationProperties(prefix = "kraken.client")
public class KrakenClientProperties {

    private String serializerType = "hessian";
    private int zkMaxWaitTime = 1000;
    private String registryAddress = "127.0.0.1:2181";
    private String zkRegisterRootPath = "kraken";




    private int connectTimeoutMillis = 2000;
    private int heartbeatIntervalTime = 4;

    private Class<? extends AbstractProxy> proxy;
    private String call;

    private LoadBalance.Type loadBalanceType = Random;
    private Compress.Type compressType = None;
//    private String loadBalance;
//    private String compress;


    private Boolean useTLS = false; // 是否开启tls加密
    private String keyPath; //私钥
    private String keyPwd; //密码
    private String certPath; //证书路径
    private String trustCertPath; //受信任ca证书路径
    private String clientAuth; //模式


    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }

    public int getZkMaxWaitTime() {
        return zkMaxWaitTime;
    }

    public void setZkMaxWaitTime(int zkMaxWaitTime) {
        this.zkMaxWaitTime = zkMaxWaitTime;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getZkRegisterRootPath() {
        return zkRegisterRootPath;
    }

    public void setZkRegisterRootPath(String zkRegisterRootPath) {
        this.zkRegisterRootPath = zkRegisterRootPath;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getHeartbeatIntervalTime() {
        return heartbeatIntervalTime;
    }

    public void setHeartbeatIntervalTime(int heartbeatIntervalTime) {
        this.heartbeatIntervalTime = heartbeatIntervalTime;
    }

    public Class<? extends AbstractProxy> getProxy() {
        return this.proxy;
    }


    public Boolean getUseTLS() {
        return useTLS;
    }

    public void setUseTLS(Boolean useTLS) {
        this.useTLS = useTLS;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getKeyPwd() {
        return keyPwd;
    }

    public void setKeyPwd(String keyPwd) {
        this.keyPwd = keyPwd;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getTrustCertPath() {
        return trustCertPath;
    }

    public void setTrustCertPath(String trustCertPath) {
        this.trustCertPath = trustCertPath;
    }

    public String getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(String clientAuth) {
        this.clientAuth = clientAuth;
    }

    public LoadBalance.Type getLoadBalanceType() {
        return loadBalanceType;
    }

    public void setLoadBalanceType(LoadBalance.Type loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }


    /*public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        loadBalance = loadBalance.trim().toLowerCase();
        switch (loadBalance) {
            case "leastactive" : {
                this.loadBalanceType = LeastActive;
                break;
            }
            case "consistenthash" : {
                this.loadBalanceType = ConsistentHash;
                break;
            }
            case "roundrobin" : {
                this.loadBalanceType = RoundRobin;
                break;
            }
            case "random" : {
                this.loadBalanceType = Random;
                break;
            }

        }
        this.loadBalance = loadBalance;
    }*/

    public Compress.Type getCompressType() {
        return compressType;
    }
    public void setCompressType(Compress.Type compressType) {
        this.compressType = compressType;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        call = call.trim().toLowerCase();
        switch (call) {
            case "oneway" :
                this.proxy = OnewayProxy.class;
                break;
            case "future" :
                this.proxy = FutureProxy.class;
                break;
            case "sync" :
                this.proxy = SyncProxy.class;
                break;
            case "callback" :
                this.proxy = CallbackProxy.class;
                break;
        }

        this.call = call;
    }

    /*public String getCompress() {
        return compress;
    }*/

    /*public void setCompress(String compress) {
        compress = compress.trim().toLowerCase();
        switch (compress) {

            case "bzip2" :
                this.compressType = Bzip2;
                break;
            case "deflater" :
                this.compressType = Deflater;
                break;
            case "gzip" :
                this.compressType = Gzip;
                break;
            case "lz4" :
                this.compressType = LZ4;
                break;
            case "lzo" :
                this.compressType = Lzo;
                break;
            case "snappy" :
                this.compressType = Snappy;
                break;
            case "none" :
                this.compressType = None;
                break;
        }

        this.compress = compress;
    }*/
}
