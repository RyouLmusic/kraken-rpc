package com.hbk.service;

import com.hbk.service.bean.Product;
import com.hbk.service.bean.User;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/25 19:53
 */
public interface HelloService {
    User getUser(String str);
    User getUser();
    Product getProduct();
}
