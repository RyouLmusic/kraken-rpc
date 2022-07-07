package com.hbk.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:41
 */
@Data
public class Good implements Serializable {
    private String name;
    private int type;
    private Food[] foods;
}
