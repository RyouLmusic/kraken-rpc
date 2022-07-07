package com.service;

import com.hbk.service.HelloService;
import com.hbk.service.bean.Product;
import com.hbk.service.bean.User;
import org.kraken.spring.annotation.KrakenService;
import org.springframework.stereotype.Service;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/25 19:56
 */
@KrakenService(version = "2", weight = 6)
public class HelloServiceImpl implements HelloService {
    @Override
    public User getUser(String str) {
        User user = new User();
        user.setAge(25);
        user.setName("kraken");
        user.setPassword("my name is kraken");
        System.out.println("******************* " + str + "  *******************");
        System.out.println(user);
        return user;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public Product getProduct() {
        Product product = new Product();
        product.setId(123);
        product.setAddress("华师附中");
        product.setCname("牛奶");
        product.setSname("燕塘牛奶");
        product.setCount(1000);
        product.setSal(67);
        return product;
    }
}
