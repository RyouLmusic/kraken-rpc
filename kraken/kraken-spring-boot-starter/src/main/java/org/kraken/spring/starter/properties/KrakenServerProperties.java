package org.kraken.spring.starter.properties;

import org.kraken.core.compress.Compress;
import org.kraken.core.remoting.net.AbstractServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static org.kraken.core.compress.Compress.Type.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 16:57
 */
@Component
@ConfigurationProperties(prefix = "kraken.server")
public class KrakenServerProperties {

    private String serializerType;

    private String registryAddress;

    private String zkRegisterRootPath;

    private int port;

    private Class<? extends AbstractServer> clazz;

    private int zkMaxWaitTime;

    private int heartbeatIntervalTime;

    private Compress.Type compressType = None;
//    private String compress;


    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Class<? extends AbstractServer> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends AbstractServer> clazz) {
        this.clazz = clazz;
    }

    public int getZkMaxWaitTime() {
        return zkMaxWaitTime;
    }

    public void setZkMaxWaitTime(int zkMaxWaitTime) {
        this.zkMaxWaitTime = zkMaxWaitTime;
    }

    public int getHeartbeatIntervalTime() {
        return heartbeatIntervalTime;
    }

    public void setHeartbeatIntervalTime(int heartbeatIntervalTime) {
        this.heartbeatIntervalTime = heartbeatIntervalTime;
    }

    public Compress.Type getCompressType() {
        return compressType;
    }
    public void setCompressType(Compress.Type compressType) {
        this.compressType = compressType;
    }
    /*public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
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
