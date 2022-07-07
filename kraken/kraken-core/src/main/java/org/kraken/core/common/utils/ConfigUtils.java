package org.kraken.core.common.utils;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.cglib.PropertiesValueInterceptor;
import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.config.NettyConfig;
import org.kraken.core.common.config.TLSConfig;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/29 17:57
 *
 * 配置的工具类：线程不安全，TODO 线程安全
 */
public class ConfigUtils {

    private final static PropertiesValueInterceptor propertiesValueInterceptor = new PropertiesValueInterceptor();
    private static AppConfig appConfig;
    private static TLSConfig rpcTLSConfig;
    private static NettyConfig rpcNettyConfig;

    /*
     * 初始化 代理类，并且使用默认文件路径
     */
    static {
        propertiesValueInterceptor.interceptorInit(Constants.CONFIGURATION_DEFAULT_PATH);
    }

    /**
     * 自定义 配置文件名称和 AppConfig.properties 和文件夹路径
     * 可以直接修改配置文件路径，每次调用都可以重新指定一个
     * @param path 配置文件路径
     * @return 对象
     */
    public static AppConfig getAppConfigBean(String path) {
        if (path.equals(propertiesValueInterceptor.getPath())) {
            return appConfig;
        }
        // 如果配置文件路径不同，就进行修改，并且在下面重新加载 InputStream
        propertiesValueInterceptor.setPath(path);
        appConfig = CglibUtils.getProxyBean(AppConfig.class, propertiesValueInterceptor);

        return appConfig;
    }

    /**
     * 默认配置文件为 AppConfig.properties
     * 使用这种的话，无需一直实例化 propertiesValueInterceptor 对象
     * @return 对象
     */
    public static AppConfig getAppConfigBeanByDefaultPath() {
        return getAppConfigBean(Constants.CONFIGURATION_DEFAULT_PATH);
    }

    /**
     * 不需要进行更新的时候，就直接返回 配置类对象
     * @return AppConfigBean
     */
    public static AppConfig getAppConfigBean() {
        if (appConfig == null)
            appConfig = CglibUtils.getProxyBean(AppConfig.class, propertiesValueInterceptor);
        return appConfig;
    }


    /**
     * TLS相关配置
     * @param path
     * @return TLSConfig
     */
    public static TLSConfig getTLSConfig(String path) {
        if (path.equals(propertiesValueInterceptor.getPath())) {
            return rpcTLSConfig;
        }
        // 如果配置文件路径不同，就进行修改，并且在下面重新加载 InputStream
        propertiesValueInterceptor.setPath(path);
        rpcTLSConfig = CglibUtils.getProxyBean(TLSConfig.class, propertiesValueInterceptor);

        return rpcTLSConfig;
    }

    /**
     * 不需要进行更新的时候，就直接返回 配置类对象
     * @return TLSConfig
     */
    public static TLSConfig getTLSConfig() {
        if (rpcTLSConfig == null)
            rpcTLSConfig = CglibUtils.getProxyBean(TLSConfig.class, propertiesValueInterceptor);
        return rpcTLSConfig;
    }



    /**
     * Netty相关配置
     * @param path
     * @return TLSConfig
     */
    public static NettyConfig getNettyConfig(String path) {
        if (path.equals(propertiesValueInterceptor.getPath())) {
            return rpcNettyConfig;
        }
        // 如果配置文件路径不同，就进行修改，并且在下面重新加载 InputStream
        propertiesValueInterceptor.setPath(path);
        rpcNettyConfig = CglibUtils.getProxyBean(NettyConfig.class, propertiesValueInterceptor);

        return rpcNettyConfig;
    }

    /**
     * 不需要进行更新的时候，就直接返回 配置类对象
     * @return TLSConfig
     */
    public static NettyConfig getNettyConfig() {
        if (rpcNettyConfig == null)
            rpcNettyConfig = CglibUtils.getProxyBean(NettyConfig.class, propertiesValueInterceptor);
        return rpcNettyConfig;
    }


    public static String makeMethodConfigKey(String methodName, Class<?>... classes) {
        StringBuilder key = new StringBuilder(methodName);
        key.append(":");
        for (Class<?> clazz : classes) {
            key.append(clazz.getName()).append("$");
        }
        return key.toString();
    }
    public static String makeServiceKey(String interfaceName, String methodName, Class<?>... classes) {
        return interfaceName + ":" + makeMethodConfigKey(methodName, classes);
    }

}
