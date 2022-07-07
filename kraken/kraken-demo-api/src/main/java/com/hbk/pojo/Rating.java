package com.hbk.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:43
 */
@Data
public class Rating implements Serializable {
    private String username;
    private String rateTime;
    private String deliveryTime;
    private String score;
    private int rateType;
    private String text;
    private String avatar;
    private String[] recommend;
}
