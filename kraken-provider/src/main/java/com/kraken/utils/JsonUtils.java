package com.kraken.utils;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/5/2 21:09
 */
public class JsonUtils {

    public static JsonReader getJsonReader(String fileName) {
        InputStream inputStream = JsonUtils.class.getResourceAsStream("/static/" + fileName);

        InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream));
        return new JsonReader(inputStreamReader);
    }




}
