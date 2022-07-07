package org.kraken.core.invoker.provider;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.config.NettyConfig;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.common.utils.CollectionUtils;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.common.utils.NetUtils;
import org.kraken.core.compress.Compress;
import org.kraken.core.loadbalance.LoadBalance;
import org.kraken.core.registry.Publisher;
import org.kraken.core.registry.zookeeper.bean.RegistryType;
import org.kraken.core.registry.zookeeper.core.ZkServicePublisher;
import org.kraken.core.remoting.enums.ResponseCodeEnum;
import org.kraken.core.remoting.net.AbstractServer;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.remoting.protocol.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/24 11:31
 *
 * 提供者门面
 */
@Slf4j
public class ProviderFactory {

    private AbstractServer server;

    private final Publisher publisher = new ZkServicePublisher();
    private URL url;
    // ----------------- 设置很多配置信息，可以顶替掉 KrakenProperties

    private final AppConfig appConfig = ConfigUtils.getAppConfigBean();
    private final NettyConfig nettyConfig = ConfigUtils.getNettyConfig();


    public void setServerConfig(ServerConfig config) {

        if (config.getPort() != 0) {
            setPort(config.getPort());
        }
        if (Objects.nonNull(config.getServerClass())){
            setServerClass(config.getServerClass());
        }
        if (Objects.nonNull(config.getRegistryAddress())) {
            setRegistryAddress(config.getRegistryAddress());
        }
        if (Objects.nonNull(config.getSerializerType())) {
            setSerializerType(config.getSerializerType());
        }
        if (config.getZkMaxWaitTime() != 0) {
            setZkMaxWaitTime(config.getZkMaxWaitTime());
        }
        if (Objects.nonNull(config.getZkRegisterRootPath())) {
            setZkRegisterRootPath(config.getZkRegisterRootPath());
        }
        if (config.getHeartbeatIntervalTime() != 0) {
            setHeartbeatIntervalTime(config.getHeartbeatIntervalTime());
        }

        if (config.getLoadBalanceType() != null) {
            setLoadBalanceType(config.getLoadBalanceType());
        }

        if (config.getCompressType() != null) {
            setCompressType(config.getCompressType());
        }

    }


    private Class<? extends AbstractServer> serverClass;
    private Integer port = appConfig.getPort(); // default port



    public void setSerializerType(String serializerType) {
        appConfig.setSerializerType(serializerType);
    }

    public void setRegistryAddress(String registryAddress) {
        appConfig.setRegistryAddress(registryAddress);
    }

    public void setZkRegisterRootPath(String zkRegisterRootPath) {
        appConfig.setZkRegisterRootPath(zkRegisterRootPath);
    }

    public void setZkMaxWaitTime(int zkMaxWaitTime) {
        appConfig.setZkMaxWaitTime(zkMaxWaitTime);
    }

    public void setHeartbeatIntervalTime(int heartbeatIntervalTime) {
        nettyConfig.setHeartbeatIntervalTime(heartbeatIntervalTime);
    }
    public void setLoadBalanceType(LoadBalance.Type type) {
        appConfig.setLoadBalanceType(type);
    }
    public void setCompressType(Compress.Type compressType) {
        appConfig.setCompressType(compressType);
    }

    public void setServerClass(Class<? extends AbstractServer> serverClass) {
        this.serverClass = serverClass;
    }

    public void setPort(Integer port) {
        this.port = port;
    }





    /*----------------------construct-------------------------*/

    public ProviderFactory() {

    }


    /*------------------------start/stop----------------------*/
    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        if (serverClass == null) {
            throw new AppException("provider server missing");
        }
        if (port == null || NetUtils.portIsUsed(port)) {
            throw new AppException("port is used: " + port);
        }

        try {
            Constructor<? extends AbstractServer> serverConstructor = serverClass.getConstructor(URL.class);
            server = serverConstructor.newInstance(url);
            server.setProviderFactory(this);
            server.open();

            /*System.out.println("---------------------+++++-----------------");
            System.out.println("server : " + server);*/
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 基本不会用到
     */
    public void stop() {
        try {
            // 关闭server
            server.close();
            // TODO 清除注册中心的地址-service
            publisher.unRegister(null, url, RegistryType.PROVIDER);

            // 清除注册的service
            CollectionUtils.mapRemoveAll(serviceMap);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /*-------------------------- 映射<ServiceKey:service> 共享的api(provider实现的 service 对象) 还需要进行初始哈------------------------------*/
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    public Map<String, Object> serviceMap() {
        return serviceMap;
    }

    public void addService(ProvideServiceConfig config, Object obj) {
        if (url == null) {
            try {
                this.url = new URL(InetAddress.getLocalHost().getHostAddress(), port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        addService(config.interfaceName, config.version, config.group, config.weight, config.warmup, obj);
    }
    /**
     * 进行添加api到容器里面
     * @param interfaceName 接口名称
     * @param version 版本
     * @param group 组
     * @param bean service类
     */
    public void addService(String interfaceName, String version, String group, int weight, int warmup, Object bean) {


        String key = makeServiceKey(interfaceName, version, group);
        serviceMap.put(key, bean);

        // 更新注册中心
//        url.setProtocol("");
        URL url_0 = new URL(url);
        url_0.setVersion(version);
        url_0.setGroup(group);
        url_0.setWeight(weight);
        url_0.setWarmup(warmup);

        publisher.register(interfaceName, url_0, RegistryType.PROVIDER);
        log.info("[kraken] provider add service success,name: {}, bean: {}", key, bean.getClass());
    }



    /**
     * invoker , called by ServerHandler
     * @param request
     * @return
     */
    public Response invokeService(Request request) {

        //  make response
        Response response = new Response();
        // match service bean
        String serviceKey = makeServiceKey(request.getInterfaceName(), request.getVersion(), request.getGroup());

        Object service = serviceMap.get(serviceKey);

        // 没有此service在此服务器上
        if (service == null) {
            return Response.fail(ResponseCodeEnum.FAIL);
        }
        // 超时
        /*if (System.currentTimeMillis() - response.getCreateMillisTime() > 3*60*1000) {
            response.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
            return response;
        }*/


        try {
            // 执行invoke
            Class<?> serviceClass = service.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParamTypes();
            Object[] parameters = request.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(service, parameters);


            response.setData(result);
        } catch (Throwable t) {
            // catch error
            log.error("xxl-rpc provider invokeService error.", t);
//            xxlRpcResponse.setErrorMsg(ThrowableUtil.toString(t));
        }

        return response;
    }

    private String makeServiceKey(String interfaceName, String version, String group) {
        String serviceKey = interfaceName;
        if (version!=null && version.trim().length() > 0) {
            serviceKey += "$".concat(version);
        }
        if (group != null && group.trim().length() > 0) {
            serviceKey += "$".concat(group);
        }
        return serviceKey;
    }


}
