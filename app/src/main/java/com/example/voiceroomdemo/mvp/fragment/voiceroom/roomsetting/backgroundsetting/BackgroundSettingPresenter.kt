/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting

import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.common.LocalDataStore
import com.example.voiceroomdemo.mvp.model.VoiceRoomListModel
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean

/**
 * @author gusd
 * @Date 2021/06/22
 */
class BackgroundSettingPresenter(val view: IBackgroundSettingView, roomInfoBean: VoiceRoomBean) :
    BaseLifeCyclePresenter<IBackgroundSettingView>(view) {

    override fun onResume() {
        super.onResume()
        view.onBackgroundList(LocalDataStore.getBackGroundUrlList())
    }
}