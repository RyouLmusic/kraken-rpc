package org.kraken.core.fiter;

import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.common.exception.InvokerException;
import org.kraken.core.remoting.protocol.Request;
import org.kraken.core.fiter.chain.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/11 15:21
 */
@Slf4j
public class GenericFilter extends Filter {
    @Override
    public Result invoke(URL url, Request request) throws AppException {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();

        // 过滤 filter method like "Object.toString()" 没有返回值，方法
        if (interfaceName.equals(Object.class.getName())) {
            log.info("[kraken] proxy class-method not support [{}#{}]", interfaceName, methodName);
            throw new InvokerException("proxy class-method not support");
        }
        return getNext().invoke(url, request);
    }

    // TODO
    /**
     * // filter for generic
     *         if (className.equals(XxlRpcGenericService.class.getName()) && methodName.equals("invoke")) {
     *
     *             Class<?>[] paramTypes = null;
     *             if (args[3]!=null) {
     *                 String[] paramTypes_str = (String[]) args[3];
     *                 if (paramTypes_str.length > 0) {
     *                     paramTypes = new Class[paramTypes_str.length];
     *                     for (int i = 0; i < paramTypes_str.length; i++) {
     *                         paramTypes[i] = ClassUtil.resolveClass(paramTypes_str[i]);
     *                     }
     *                 }
     *             }
     *
     *             className = (String) args[0];
     *             varsion_ = (String) args[1];
     *             methodName = (String) args[2];
     *             parameterTypes = paramTypes;
     *             parameters = (Object[]) args[4];
     *         }
     */
}
