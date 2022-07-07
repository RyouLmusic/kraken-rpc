package com.kraken.service;

import com.hbk.pojo.Good;
import com.hbk.pojo.Rating;
import com.hbk.pojo.Seller;
import com.hbk.service.ShowInfoService;
import com.kraken.mapper.ShowInfoMapper;
import org.kraken.spring.annotation.KrakenService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 20:59
 */
@KrakenService(version = "1.0")
public class ShowInfoServiceImpl implements ShowInfoService {

    private final ShowInfoMapper showInfoMapper;

    public ShowInfoServiceImpl(ShowInfoMapper showInfoMapper) {
        this.showInfoMapper = showInfoMapper;
    }

    @Override
    public Seller showSeller() {
        return showInfoMapper.showSeller();
    }

    @Override
    public List<Good> showGoods() {
        return showInfoMapper.showGoods();
    }

    @Override
    public List<Good> showGoods(int type) {
        return showInfoMapper.showGoods(type);
    }

    @Override
    public List<Rating> showRatings() {
        return showInfoMapper.showRatings();
    }

    @Override
    public List<Rating> showRatings(int type) {
        return showInfoMapper.showRatings(type);
    }
}
