package org.kraken.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/26 16:17
 * 配置文件中的属性信息
 * 如：
 * @ PropertiesValue(property="serializerType", defaultVale="")
 * private String serializerType;
 *
 */
@Target({ElementType.FIELD , ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertiesValue {
    /**
     * 配置文件中 通过此属性进行区分配置
     */
    String property() default "";

    /**
     * 一个配置文件中的属性的默认值
     */
    String defaultValue() default "";
}
