package com.hbk.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 14:42
 */

public class SpringInvoker {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(InvokerConfig.class);
        context.refresh();

        HelloController helloController = (HelloController) context.getBean("helloController");
        helloController.hello();
    }
}
