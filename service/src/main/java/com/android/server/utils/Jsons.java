package com.android.server.utils;

import com.google.gson.Gson;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;

/**
 * Created by kiddo on 17-7-14.
 */

public class Jsons {
    private static final Logger logger = LoggerFactory.getLogger(Jsons.class);

    public static String toJson(Object bean){
        Gson gson = new Gson();
        logger.debug("json to json:" + bean, gson.toJson(bean));
        return gson.toJson(bean);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {

        Gson gson = new Gson();
        T bean = gson.fromJson(json, clazz);
        return bean;
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        try {
            return fromJson(new String(json, "UTF-8"), clazz);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
