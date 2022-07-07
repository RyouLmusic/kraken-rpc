package org.kraken.spring.starter.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/29 10:42
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAnnotationCondition.class)
public @interface ConditionalOnAnnotation {
    Class<?>[] value() default {};
}
