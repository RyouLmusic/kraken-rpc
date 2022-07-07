package org.kraken.core.invoker.proxy;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.config.AppConfig;
import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.common.utils.ConfigUtils;
import org.kraken.core.common.utils.IdGeneratorUtil;
import org.kraken.core.common.utils.StringUtils;
import org.kraken.core.compress.Compress;
import org.kraken.core.invoker.InvokerFactory;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.invoker.config.Config;
import org.kraken.core.invoker.config.DefaultConfig;
import org.kraken.core.invoker.config.MethodConfig;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.loadbalance.LoadBalance;
import org.kraken.core.registry.Subject;
import org.kraken.core.registry.zookeeper.bean.RegistryType;
import org.kraken.core.registry.zookeeper.bean.ZkNode;
import org.kraken.core.registry.zookeeper.core.ZkServiceDiscover;
import org.kraken.core.remoting.enums.MessageTypeEnum;
import org.kraken.core.remoting.net.AbstractClient;
import org.kraken.core.remoting.net.netty.client.ConnectClientPool;
import org.kraken.core.remoting.protocol.ProtocolHeader;
import org.kraken.core.remoting.protocol.ProtocolMessage;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.serializer.SerializerContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.kraken.core.common.utils.ConfigUtils.makeServiceKey;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/21 20:25
 */
@Slf4j
public abstract class AbstractProxy implements MethodInterceptor {

    private final Enhancer enhancer = new Enhancer();

    protected AbstractClient client;
    protected InvokerFactory invokerFactory;
    private final Class<? extends AbstractClient> clientClass;
    private final ReferenceBean reference;
    private final AppConfig config = ConfigUtils.getAppConfigBean();

    private Request refRequest;

    public AbstractProxy(Class<? extends AbstractClient> clientClass, ReferenceBean reference) throws Throwable {
        // init Client
        this.clientClass = clientClass;
        this.reference = reference;
    }



    /**
     * 实际进行的操作： 发送消息到服务端，接收来自服务端的响应，然后返回结果
     * @param message msg
     * @return invoker result
     * @throws Throwable 异常
     */
    protected abstract Object doIntercept(ProtocolMessage message) throws Throwable;


    /**
     * 重写方法拦截在方法前和方法后加入  对注解的解析
     * Object obj为目标对象 此目标对象的地址为com.kraken.common.bean.AppConfig$$EnhancerByCGLIB$$e3b0eef0，会加上 $$EnhancerByCGLIB$$  $$是前缀和后缀
     * Method method为目标方法
     * Object[] params 为参数，
     * MethodProxy proxy CGlib方法代理对象
     */
    @Override
    public Object intercept(Object object, Method method, Object[] params, MethodProxy proxy) throws Throwable {
        if (invokerFactory == null) {
            invokerFactory = InvokerFactory.getInstance();
        }
        // 设置回调方式的callback方法
        if (reference.getInvokeCallback() != null) {
            InvokeCallback.setInvokeCallback(reference.getInvokeCallback());
        }
        // method param
        String interfaceName = method.getDeclaringClass().getName();	// iface.getName()
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();


        // 初始化config
        initConfig(interfaceName, methodName, parameterTypes);

        ProtocolMessage message = builtMessage(interfaceName, methodName, parameterTypes, params);
        Request request = (Request) message.getBody();
        this.refRequest = request;
        //---------------初始化client----------------
        URL url_0 = getProviderAddress();
        URL url = new URL("10.102.82.92" , 8002);
        this.client = ConnectClientPool.getPool(url_0, clientClass, reference, this);
//        System.out.println("---------------------+++++-----------------");
//        System.out.println("client  :" + client);
        client.getUrl().setActive(active_0);
        client.getUrl().setTimeout(timeout_0);

        invokeFilterChain(request);

        return doIntercept(message);
    }


    public AppConfig getConfig() {
        return config;
    }

    public String getVersion() {
        return version;
    }

    public String getGroup() {
        return group;
    }

    public void setInvokerFactory(InvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }

    protected ProtocolMessage builtMessage(String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] params) {
        /*------------------ 构建 Message request --------------*/

        ProtocolMessage message = new ProtocolMessage();

        // 构建头部
        Long seq = IdGeneratorUtil.getId();
        byte codec = SerializerContext.SerializerType.getKeyByValue(getConfig().getSerializerType());
        Compress.Type compressType = config.getCompressType();
        ProtocolHeader header = ProtocolHeader
                .builder()
                .type(MessageTypeEnum.REQUEST.getValue())
                .reserve((short) 0)
                .seq(seq)
                .codec(codec)
                .serialize(codec)
                .compress(compressType.getCode())
                .version(Constants.H_RPC_VERSION)
                .build();

        // 构建body
        Request body = Request
                .builder()
                .interfaceName(interfaceName)
                .methodName(methodName)
                .paramTypes(parameterTypes)
                .parameters(params)
                .group(getGroup())
                .version(version)
                .build();

        message.setHeader(header);
        message.setBody(body);

        return message;
    }

    /**
     * 执行filter chain
     * @param request request
     */
    private void invokeFilterChain(Request request) {

        client.getUrl().setActive(active_0);
//        System.out.println(active_0);
        // 调用
        invokerFactory.invoke(client.getUrl(), request);
    }

    public <T> T getInstance(Class<?> clazz) {
        // 设置哪个类需要代理
        enhancer.setSuperclass(clazz);
        // 设置怎么代理
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }



    /*---------------------------reference/method config------------------------------*/
    /**
     * 一个interface全局的配置
     */
    private ReferenceConfig referenceConfig;
    /**
     * 每个方法都有自己的配置
     */
    private Map<String, MethodConfig> methodConfigMap;

    public void setReferenceConfig(ReferenceConfig referenceConfig) {
        this.referenceConfig = referenceConfig;
    }



    private MethodConfig getMethodConfig(String interfaceName, String methodName, Class<?>... classes) {
        String key = makeServiceKey(interfaceName, methodName, classes);
        return methodConfigMap.get(key);
    }

    public void setMethodConfigMap(Map<String, MethodConfig> methodConfigMap) {
        this.methodConfigMap = methodConfigMap;
    }


    /*-----------------------init config-----------------------------*/

    private int active_0;
    private long timeout_0;
    private String version;
    private String group;
    // TODO 其他配置

    public void initConfig(String interfaceName, String methodName, Class<?>[] classes) {

        MethodConfig methodConfig = getMethodConfig(interfaceName, methodName, classes);

        // 默认配置
        setConfig(new DefaultConfig());
        // 全局配置 覆盖默认配置
        setConfig(referenceConfig);
        // 单独接口配置 覆盖以上配置
        setConfig(methodConfig);
    }

    private void setConfig(Config config) {

        if (Objects.nonNull(config)) {
            if (config.getActive() != 0) {
                active_0 = config.getActive();
            }
            if (config.getTimeout() != 0) {
                timeout_0 = config.getTimeout();
            }
            if (!StringUtils.isEmpty(config.getVersion())) {
                version = config.getVersion();
            }
            // TODO 其他配置
        }

    }

    /**
     * 获取方法调用的远程连接地址
     * @return 返回URL
     */
    public URL getProviderAddress() {

        String interfaceName = refRequest.getInterfaceName();
        URL url = null;
        final LoadBalance balance = LoadBalance.getInstance();

        Subject subject = new ZkServiceDiscover();
        // TODO 改成set
        List<URL> urls = subject.subject(interfaceName, RegistryType.PROVIDER);

        if (urls == null || urls.isEmpty()) {
            throw new RemotingException("[kraken] not found service:" + interfaceName + " address");
        }
        if (urls.size() == 1) {
            url = urls.get(0);
            if (url == null || url.getAddress().trim().length()==0) {
                throw new RemotingException("[kraken] reference bean["+ interfaceName +"] address empty");
            }
            subject.watcher((curatorFramework, pathChildrenCacheEvent) -> {

                // 进行监听
            }, ((ZkNode) url).getPath());
            return url;
        } else {
            // load balance
            return balance.select(urls, refRequest);
        }

//        return new URL("127.0.0.1" , 8002);
    }

}
