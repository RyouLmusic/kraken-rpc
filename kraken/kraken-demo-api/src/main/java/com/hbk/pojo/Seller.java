package com.hbk.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:35
 */
@Data
public class Seller implements Serializable {
    private String name;
    private String description;
    private int deliveryTime;
    private double score;
    private double serviceScore;
    private double foodScore;
    private double rankRate;
    private double minPrice;
    private double deliveryPrice;
    private int ratingCount;
    private int sellCount;
    private String bulletin;
    private Support[] supports;
    private String avatar;
    private String[] pics;
    private String[] bigPics;
    private String[] infos;

}
