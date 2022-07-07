package org.kraken.spring.starter.annotation;

import org.kraken.spring.starter.config.KrakenServerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/29 14:02
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(KrakenServerAutoConfiguration.class)
public @interface EnableAutoKrakenServer {
}
