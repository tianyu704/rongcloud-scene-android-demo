/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.net

import cn.rongcloud.voiceroomdemo.net.api.CommonApiService
import cn.rongcloud.voiceroomdemo.net.api.DownloadFileApiService
import com.rongcloud.common.net.RetrofitManager

/**
 * @author gusd
 * @Date 2021/06/07
 */
private const val TAG = "RetrofitManager"

object VoiceRoomNetManager {
    val voiceRoomService: CommonApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RetrofitManager.getRetrofit().create(CommonApiService::class.java)
    }

    val downloadService: DownloadFileApiService by lazy {
        RetrofitManager.getDownloadRetrofit().create(DownloadFileApiService::class.java)
    }
}