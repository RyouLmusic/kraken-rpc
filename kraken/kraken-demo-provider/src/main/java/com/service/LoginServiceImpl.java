package com.service;

import com.hbk.service.LoginService;
import com.hbk.service.bean.User;
import org.kraken.spring.annotation.KrakenService;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/12 19:11
 */
@KrakenService(version = "2", weight = 6)
public class LoginServiceImpl implements LoginService {

    @Override
    public User getUser() {
        User user = new User();
        user.setAge(25);
        user.setName("kraken");
        user.setPassword("LoginServiceImpl");
        return user;
    }
}
