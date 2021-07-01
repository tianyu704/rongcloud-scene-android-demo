/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import com.google.gson.Gson;

import cn.rongcloud.voiceroom.utils.JsonUtils;

/**
 * @author gusd
 * @Date 2021/06/03
 */
public abstract class BaseInfo {

    public String toJson() {
        return JsonUtils.toJson(this);
    }

}
