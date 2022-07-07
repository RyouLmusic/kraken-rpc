package com.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/21 22:41
 */
public class SpringProvider {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ProviderConfig.class);
        context.refresh();
    }
}
