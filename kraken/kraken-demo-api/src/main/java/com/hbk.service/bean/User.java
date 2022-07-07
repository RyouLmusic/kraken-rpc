package com.hbk.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/25 19:54
 */
@Data
public class User implements Serializable {
    private String name;
    private String password;
    private Integer age;
}
