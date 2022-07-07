package org.kraken.spring.starter.annotation;

import org.kraken.spring.starter.config.KrakenClientAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 12:43
 * 允许启动
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(KrakenClientAutoConfiguration.class)
public @interface EnableAutoKrakenClient {
}
