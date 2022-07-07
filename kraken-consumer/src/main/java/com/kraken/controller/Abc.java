package com.kraken.controller;

import com.hbk.service.bean.User;
import org.kraken.core.invoker.call.InvokeCallback;
import org.kraken.core.remoting.protocol.Response;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/1 16:26
 */
public class Abc extends InvokeCallback<User> {

    public Abc() {}
    @Override
    public void onSuccess(User result) {
        System.out.println(result + " ---------2-------- ");
    }

    @Override
    public void onFailure(Response response, Throwable exception) {

    }

}
