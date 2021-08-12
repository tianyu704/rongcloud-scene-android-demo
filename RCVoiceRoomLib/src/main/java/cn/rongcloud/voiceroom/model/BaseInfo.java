/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.model;

import cn.rongcloud.voiceroom.utils.JsonUtils;

/**
 * 实体基类
 */
public abstract class BaseInfo {

    public String toJson() {
        return JsonUtils.toJson(this);
    }

}
