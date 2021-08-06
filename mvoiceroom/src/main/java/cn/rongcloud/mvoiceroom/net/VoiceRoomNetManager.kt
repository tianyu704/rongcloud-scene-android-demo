/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.mvoiceroom.net

import cn.rongcloud.mvoiceroom.net.api.GiftApiService
import cn.rongcloud.mvoiceroom.net.api.MusicApiService
import cn.rongcloud.mvoiceroom.net.api.VoiceRoomApiService
import com.rongcloud.common.net.RetrofitManager

/**
 * @author gusd
 * @Date 2021/06/07
 */
private const val TAG = "RetrofitManager"

object VoiceRoomNetManager {

    val voiceRoomService: VoiceRoomApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RetrofitManager.getRetrofit().create(VoiceRoomApiService::class.java)
    }

    val musicService: MusicApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RetrofitManager.getRetrofit().create(MusicApiService::class.java)
    }

    val giftService: GiftApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RetrofitManager.getRetrofit().create(GiftApiService::class.java)
    }
}