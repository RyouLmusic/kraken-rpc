package com.hbk.common;

import com.hbk.service.HelloService;
import com.hbk.service.bean.User;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.compress.Compress;
import org.kraken.core.fiter.Filter;
import org.kraken.core.fiter.chain.Result;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.invoker.config.MethodConfig;
import org.kraken.core.invoker.config.ReferenceConfig;
import org.kraken.core.invoker.proxy.CallbackProxy;
import org.kraken.core.invoker.proxy.SyncProxy;
import org.kraken.core.invoker.reference.ReferenceBean;
import org.kraken.core.remoting.net.netty.client.NettyClient;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.remoting.protocol.Response;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 16:04
 */
public class Reference {
    public static void main(String[] args) {
        try {
            sync();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void sync() throws Throwable {
        ReferenceBean reference = new ReferenceBean();
        // 注册服务
        reference.setClazz(HelloService.class);
        // 选择通信方式
        reference.setClientClass(NettyClient.class);
        // 设置调用方式
        reference.setProxyClass(SyncProxy.class);

        reference.addFilter(new Filter() {
            @Override
            public Result invoke(URL url, Request request) throws AppException {
                System.out.println(request + " : ------------------------ ");

                return getNext().invoke(url, request);
            }
        });


        /*--配置 ReferenceConfig 和 MethodConfig 都进行设置，生效的是MethodConfig-*/
        // 服务接口所有方法配置
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setActive(11);
        referenceConfig.setVersion("2");
        // 指定服务方法的配置
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setActive(20);
        methodConfig.setVersion("1.0");
        referenceConfig.setCompressType(Compress.Type.Gzip);
        reference.setReferenceConfig(referenceConfig);
        reference.putMethodConfig(methodConfig, HelloService.class.getName(), "getUser", String.class);


        HelloService service = reference.getObject();
        System.out.println(service.getUser("123"));

        /*LoginService loginService = reference.getObject(LoginService.class);
        System.out.println(loginService.getUser());*/



        /*Product product = service.getProduct();
        System.out.println(product);*/

//        reference.stop();
    }

    public static void callback() throws Throwable {
        ReferenceBean reference = new ReferenceBean();
        // 注册服务
        reference.setClazz(HelloService.class);
        // 选择通信方式
        reference.setClientClass(NettyClient.class);
        // 设置调用方式
        reference.setProxyClass(CallbackProxy.class);


        /*--配置 ReferenceConfig 和 MethodConfig 都进行设置，生效的是MethodConfig-*/
        // 服务接口所有方法配置
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setActive(11);
        referenceConfig.setVersion("2");
        // 指定服务方法的配置
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setActive(20);
        methodConfig.setVersion("2");

        reference.setReferenceConfig(referenceConfig);
        reference.putMethodConfig(methodConfig, HelloService.class.getName(), "getUser", String.class);


        HelloService service = reference.getObject();

        InvokeCallback.setInvokeCallback(new InvokeCallback<User>() {
            @Override
            public void onSuccess(User result) {
                System.out.println(result);

                System.out.println("ccc");
            }

            @Override
            public void onFailure(Response response, Throwable exception) {
                System.out.println(".....");
            }
        });


        System.out.println("service.getUser(\"123\")" + service.getUser("123"));
    }
}
