/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.net.api.bean.request
import com.google.gson.annotations.SerializedName


/**
 * @author gusd
 * @Date 2021/06/23
 */
data class RoomNameRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("roomId")
    val roomId: String
)