/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.functionmodel

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.IRoomSettingView

/**
 * @author gusd
 * @Date 2021/07/05
 */
class MusicFunction(roomId: String, val view: IRoomSettingView) :
    BaseRoomSettingFunctionModel(roomId) {
    override fun onCreate() {
        onDataChange(R.drawable.ic_room_setting_music, "音乐") {
            view.showMusicSettingFragment()
        }
    }
}