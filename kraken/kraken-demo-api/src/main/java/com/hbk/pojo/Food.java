package com.hbk.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:41
 */
@Data
public class Food implements Serializable {
    private String name;
    private String price;
    private String oldPrice;
    private String description;
    private String sellCount;
    private String rating;
    private String info;
    private Rating[] ratings;
    private String icon;
    private String image;

}
