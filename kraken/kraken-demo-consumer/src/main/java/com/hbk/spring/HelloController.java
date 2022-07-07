package com.hbk.spring;

import com.hbk.service.HelloService;
import com.hbk.service.bean.User;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.remoting.protocol.Response;
import org.kraken.spring.annotation.KrakenReference;
import org.springframework.stereotype.Controller;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/23 14:45
 */
@Controller
public class HelloController {
    @KrakenReference(version = "1.0" , success = "onSuccess()", failure = "onFailure()", callback = "com.hbk.spring.HelloController$abc")
    HelloService helloService;


    public void hello() {
        System.out.println(helloService.getUser("abc"));
    }

    class abc extends InvokeCallback<User> {
        @Override
        public void onSuccess(User result) {
            System.out.println(result + " ---------3-------- ");
        }

        @Override
        public void onFailure(Response response, Throwable exception) {

        }
    }

}


