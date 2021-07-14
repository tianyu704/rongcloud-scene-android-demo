/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author gusd
 * @Date 2021/06/04
 */
public class JsonUtils {
    private static Gson gson;

    static {
        gson = new Gson();
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        return gson.fromJson(jsonString, clazz);
    }
}
