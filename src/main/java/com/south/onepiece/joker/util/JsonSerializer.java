package com.south.onepiece.joker.util;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * json 工具类 fastjson
 *
 * @author zhangwenming
 * @date 2016/10/17 19:08
 * version: 1.0
 */
public class JsonSerializer {

    public static <T> T parseJson(String json, Class<?> clazz) {
        return (T) JSON.parseObject(json, clazz);
    }

    public static <T> List<T> parseJsonList(String json, Class<?> clazz) {
        return (List<T>) JSON.parseArray(json, clazz);
    }

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

}
