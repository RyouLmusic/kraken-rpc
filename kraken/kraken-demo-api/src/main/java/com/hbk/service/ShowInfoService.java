package com.hbk.service;

import com.hbk.pojo.Good;
import com.hbk.pojo.Rating;
import com.hbk.pojo.Seller;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:48
 */
public interface ShowInfoService {

    Seller showSeller();
    List<Good> showGoods();
    List<Good> showGoods(int type);
    List<Rating> showRatings();
    List<Rating> showRatings(int type);
}
