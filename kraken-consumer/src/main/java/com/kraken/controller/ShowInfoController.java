package com.kraken.controller;

import com.hbk.pojo.Good;
import com.hbk.pojo.Rating;
import com.hbk.pojo.Seller;
import com.hbk.service.ShowInfoService;
import org.kraken.spring.annotation.KrakenReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 22:23
 */
@Controller
@RequestMapping("/kraken/info")
@ResponseBody
public class ShowInfoController {

    @KrakenReference(version = "1.0")
    private ShowInfoService showInfoService;

    @RequestMapping("/seller")
    public Seller showSeller() {
        return showInfoService.showSeller();
    }

    @RequestMapping("/good")
    public List<Good> showGoods() {
        return showInfoService.showGoods();
    }

    @RequestMapping("/good/{type}")
    public List<Good> showGoods(@PathVariable("type") int type) {
        return showInfoService.showGoods(type);
    }

    @RequestMapping("/rating")
    public List<Rating> showRating() {
        return showInfoService.showRatings();
    }

    @RequestMapping("/rating/{type}")
    public List<Rating> showRating(@PathVariable("type") int type) {
        return showInfoService.showRatings(type);
    }

}
