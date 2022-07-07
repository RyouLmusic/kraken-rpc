package com.hbk.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:39
 */
@Data
public class Support implements Serializable {
    private int type;
    private String description;
}
