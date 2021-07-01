/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.net.api.bean.request

/**
 * @author gusd
 * @Date 2021/06/07
 */
data class LoginRequestBean(
    val mobile: String,
    val verifyCode: String,
    val userName: String? = null,
    val portrait: String? = null,
    val deviceId: String
)
