package org.kraken.spring.annotation;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/4/28 20:20
 * 带有此注释，在请求之前先执行此类
 *
 * reference.addFilter(new Filter() {
 *             @Override
 *             public Result invoke(URL url, Request request) throws AppException {
 *                 System.out.println(request + " : ------------------------ ");
 *
 *                 return getNext().invoke(url, request);
 *             }
 *         });
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface KrakenFilter {
    String value() default "";
}
