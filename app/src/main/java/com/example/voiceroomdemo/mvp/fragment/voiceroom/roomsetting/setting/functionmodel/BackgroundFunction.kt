/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.functionmodel

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.IRoomSettingView

/**
 * @author gusd
 * @Date 2021/06/23
 */
class BackgroundFunction(val view:IRoomSettingView) :BaseFunctionModel() {
    override fun onCreate() {
        onDataChange(R.drawable.ic_room_setting_background,"房间背景"){
            view.showBackgroundFragment()
        }
    }
}