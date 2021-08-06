/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.net

import cn.rongcloud.voiceroomdemo.net.api.CommonApiService
import cn.rongcloud.voiceroomdemo.net.api.GiftApiService
import cn.rongcloud.voiceroomdemo.net.api.MusicApiService
import cn.rongcloud.voiceroomdemo.net.api.VoiceRoomApiService
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