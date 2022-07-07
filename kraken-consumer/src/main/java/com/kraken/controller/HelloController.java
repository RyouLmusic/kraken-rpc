package com.kraken.controller;

import com.hbk.service.HelloService;
import com.hbk.service.bean.User;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.remoting.protocol.Response;
import org.kraken.spring.annotation.KrakenReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/29 15:41
 */
@Controller
public class HelloController {

    @KrakenReference(version = "1.0", callback = "com.kraken.controller.HelloController$Callback")
    private HelloService helloService;

    @ResponseBody
    @RequestMapping("/hello")
    public User hello() {
        User user = helloService.getUser("123");
        System.out.println("uer -- " + user);
//        User user = new User();
//        user.setName("hnk");
        return user;
    }

    static class Callback extends InvokeCallback<User> {

        @Override
        public void onSuccess(User user) {
            System.out.println("uer2 -- " + user);
        }

        @Override
        public void onFailure(Response response, Throwable throwable) {

        }
    }
}
