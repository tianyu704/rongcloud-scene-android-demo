/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.net.api.bean.request

/**
 * @author gusd
 * @Date 2021/06/07
 */
data class GetVerificationCodeInternational(
    val mobile: String,
    val region: String
)
