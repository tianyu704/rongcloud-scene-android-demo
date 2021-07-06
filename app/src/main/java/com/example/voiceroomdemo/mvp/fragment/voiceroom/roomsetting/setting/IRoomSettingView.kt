/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting

import com.example.voiceroomdemo.common.IBaseView
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

/**
 * @author gusd
 * @Date 2021/06/22
 */
interface IRoomSettingView :IBaseView {
    fun showPasswordDialog(): Maybe<String>?{
        return null
    }

    fun showModifyRoomNameDialog(roomName: String?):Maybe<String>?{
        return null
    }

    fun showBackgroundFragment()
    fun hideSettingView(){}
    fun showMusicSettingFragment(){}
}