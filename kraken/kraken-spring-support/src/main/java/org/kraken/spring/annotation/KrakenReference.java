package org.kraken.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 9:46
 * 用于消费端
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface KrakenReference {

    /**
     * 设置接口的
     */
    int active() default 20;

    /**
     * 这个接口在调用的时候，可以接受的timeout
     */
    long timeout() default 1000;
    /**
     * 请求的service版本
     */
    String version() default "1.0";


    String success() default "";
    String failure() default "";

    String callback() default "";
}
