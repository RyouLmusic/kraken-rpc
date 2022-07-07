package com.kraken.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.hbk.pojo.Good;
import com.hbk.pojo.Rating;
import com.hbk.pojo.Seller;
import com.kraken.utils.JsonUtils;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 21:03
 */
@Repository
public class ShowInfoMapper {
    Gson gson = new Gson();

    public Seller showSeller() {
        JsonReader jsonReader = JsonUtils.getJsonReader("seller.json");

        Type type = new TypeToken<Seller>(){}.getType();
        return gson.fromJson(jsonReader, type);
    }

    public List<Good> showGoods() {
        JsonReader jsonReader = JsonUtils.getJsonReader("goods.json");
        Type type = new TypeToken<List<Good>>(){}.getType();
        return gson.fromJson(jsonReader, type);
    }

    public List<Good> showGoods(int type) {
        JsonReader jsonReader = JsonUtils.getJsonReader("goods.json");
        List<Good> goods = gson.fromJson(jsonReader, new TypeToken<List<Good>>(){}.getType());

        return goods.
                stream().
                filter((good -> good.getType() == type)).
                collect(Collectors.toList());
    }

    public List<Rating> showRatings() {
        JsonReader jsonReader = JsonUtils.getJsonReader("ratings.json");
        Type type = new TypeToken<List<Rating>>(){}.getType();
        return gson.fromJson(jsonReader, type);
    }

    public List<Rating> showRatings(int type) {
        JsonReader jsonReader = JsonUtils.getJsonReader("ratings.json");
        List<Rating> ratings = gson.fromJson(jsonReader, new TypeToken<List<Rating>>(){}.getType());

        return ratings.
                stream().
                filter((rating -> rating.getRateType() == type)).
                collect(Collectors.toList());
    }

    public Seller seller() throws IOException {
        String path = Objects.requireNonNull(this.getClass().getResource("/static/seller.json")).getPath().substring(1);
        String s = new String(Files.readAllBytes(Paths.get(path)));

        System.out.println(s);

        Seller seller = gson.fromJson(s, Seller.class);
        return seller;
    }

    public static void main(String[] args) throws IOException {
        ShowInfoMapper mapper = new ShowInfoMapper();
        Seller seller = mapper.showSeller();

        System.out.println(seller);

        List<Good> goods = mapper.showGoods(-1);

        goods.forEach((System.out::println));
        System.out.println("---------------");
        List<Rating> ratings = mapper.showRatings(0);

        ratings.forEach((System.out::println));
    }
}
