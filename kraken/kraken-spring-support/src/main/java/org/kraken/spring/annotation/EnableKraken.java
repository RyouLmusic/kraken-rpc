package org.kraken.spring.annotation;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 12:43
 * 允许启动
 */
@Deprecated
public @interface EnableKraken {

    String[] packages() default {};
}
