package org.kraken.core.common.cglib;

import org.kraken.core.common.annotation.PropertiesValue;
import org.kraken.core.common.config.ObtainingConfigurationInfo;
import org.kraken.core.common.exception.ConfigLoadException;
import org.kraken.core.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.kraken.core.compress.Compress;
import org.kraken.core.loadbalance.LoadBalance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/26 20:08
 *
 * CGLIB:拦截器，对AppConfigurationBean类进行增强
 *
 * Enhancer enhancer =new Enhancer();
 * enhancer.setSuperclass(TargetObject.class);
 * enhancer.setCallback(new TargetInterceptor());
 * TargetObject targetObject2=(TargetObject)enhancer.create();
 */
@Slf4j
public class PropertiesValueInterceptor implements MethodInterceptor {

    private final static Set<String> PROPERTIES_VALUE_SET = Collections.synchronizedSet(new HashSet<>());
    private ObtainingConfigurationInfo oci;

    public void interceptorInit(String path) {
        oci = new ObtainingConfigurationInfo();
        oci.setConfigFilePath(path);
        oci.initObtain();
    }

    /**
     * 重写方法拦截在方法前和方法后加入  对注解的解析
     * Object obj为目标对象 此目标对象的地址为com.kraken.common.bean.AppConfig$$EnhancerByCGLIB$$e3b0eef0，会加上 $$EnhancerByCGLIB$$  $$是前缀和后缀
     * Method method为目标方法
     * Object[] params 为参数，
     * MethodProxy proxy CGlib方法代理对象
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] params, MethodProxy proxy) throws Throwable {
        // 如果通过 set的方法进行设置的话，抛弃properties的配置
        Object result = proxy.invokeSuper(obj, params);
        // 获取返回的方法类型
        Class<?> returnType = method.getReturnType();
        // TODO EL表达式
        if (Objects.nonNull(result) && !returnType.equals(int.class)) {
            return result;
        }


        String methodName = method.getName();
        // 排除非 获取类型的方法，不需要进行增强
        if (!methodName.startsWith("is") && !methodName.startsWith("get")) return proxy.invokeSuper(obj, params);

        String fieldName;

        if (returnType.equals(boolean.class)) {
            fieldName = StringUtils.toLowercaseFirst(methodName.substring("is".length())).trim();
        } else {
            // 非boolean类型的返回
            fieldName = StringUtils.toLowercaseFirst(methodName.substring("get".length())).trim();
        }

        // 通过属性名称获取到类属性
        Field field;
        try {
            // 边界处理
            // 直接使用 obj.getClass().getDeclaredField(fieldName); 会出现NoSuchFieldException异常
            // 因为此对象并不在原来的对象了，地址已经更改了
            // com.kraken.common.config.AppConfig$$EnhancerByCGLIB$$e3b0eef0
            String className = obj.getClass().getTypeName();
            // 截取原来的类名
            className = className.substring(0, className.indexOf("$$"));

            Class<?> clazz = Class.forName(className);
            field = clazz.getDeclaredField(fieldName);
//            field.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new ConfigLoadException("配置信息类的["+ methodName + "()]的get方法名称没有和属性名称保持一致");
        }


        // 通过属性获取到 其注释的信息
        PropertiesValue value = field.getAnnotation(PropertiesValue.class);
        // 如果没有注解,直接返回空值
        if (value == null) return null;
        String propertyKey = value.property();
        String property = oci.getProperty(propertyKey, value.defaultValue());

        // 返回默认值
        if (StringUtils.isEmpty(property)){
            property = value.defaultValue();
        }

        /*if (!PROPERTIES_VALUE_SET.contains(propertyKey)) {
            log.info("[kraken] 加载{}配置为 - {}", propertyKey, property);
            PROPERTIES_VALUE_SET.add(propertyKey);
        }*/
        // 进行常见的类型转换
        // 基本原始类型，这里只写了部分类型，如遇项目有多重类型，可以添加补全所以类型
        if (returnType.isPrimitive()){
            if (returnType.equals(int.class)){ return Integer.valueOf(property);}
            else if (returnType.equals(long.class)){ return (Long.valueOf(property));}
            else if (returnType.equals(double.class)) {return (Double.valueOf(property));}
            else if (returnType.equals(float.class)) { return (Float.valueOf(property)); }
            else if (returnType.equals(boolean.class)) { return (Boolean.valueOf(property));}
        }else {
            if (returnType.equals(Integer.class)){ return Integer.valueOf(property);}
            else if (returnType.equals(String.class)){ return String.valueOf(property);}
            else if (returnType.equals(Boolean.class)){ return Boolean.valueOf(property);}
            else if (returnType.equals(LoadBalance.Type.class)) {return LoadBalance.Type.valueOf(property);}
            else if (returnType.equals(Compress.Type.class)) return Compress.Type.valueOf(property);
        }

        return property;
    }


    public void setPath(String path) {
        oci.setConfigFilePath(path);
    }

    public String getPath() {
        return oci.getConfigFilePath();
    }
}
