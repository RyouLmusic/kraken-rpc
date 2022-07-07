package org.kraken.core.common.utils;


import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * @ Author: 汉高鼠刘邦
 * @ Date: 2022/1/26 20:13
 */
public class CglibUtils {

    private final static Enhancer enhancer = new Enhancer();



    /**
     * 直接返回 动态代理类
     * @param clazz 被代理的类
     * @param interceptor 设置的CGLIB拦截器(在这里定义增强效果)
     * @param <T> 设置的类型
     * @return 返回代理类对象实例
     */
    public static <T> T getProxyBean(Class<T> clazz, MethodInterceptor interceptor) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(interceptor);
        return (T)enhancer.create();
    }

    public static Object getProxyBean(MethodInterceptor interceptor) {
        enhancer.setCallback(interceptor);
        return enhancer.create();
    }


}
