/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.net.api.bean.request
import com.google.gson.annotations.SerializedName


/**
 * @author gusd
 * @Date 2021/06/17
 */
data class MusicOrderRequest(
    @SerializedName("roomId")
    val roomId: String? = null,
    @SerializedName("fromId")
    val fromId: Int? = null,
    @SerializedName("toId")
    val toId: Int? = null
)