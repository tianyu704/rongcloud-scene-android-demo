/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity.iview

import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomListBean
import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean

/**
 * @author gusd
 * @Date 2021/06/09
 */
interface IVoiceRoomListView :IBaseView {
    fun onDataChange(list: List<VoiceRoomBean>?)
    fun onLoadError(throwable: Throwable?)
    fun showInputPasswordDialog(bean: VoiceRoomBean)
}