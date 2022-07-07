package com.hbk.service.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/1 17:17
 */
@Data
public class Product implements Serializable {

    private int id;
    private String sname;//商品名
    private String cname;//类别名
    private int sal;//单价
    private int count;//库存量
    private String address;//产地

}
