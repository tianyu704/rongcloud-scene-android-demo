/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId

/**
 * @author gusd
 * @Date 2021/06/24
 */
class SeatOrderOperationPresenter(view: IViewPageListView,val roomId: String) :
    BaseLifeCyclePresenter<IViewPageListView>(view) {

    override fun onCreate() {
        super.onCreate()
        getVoiceRoomModelByRoomId(roomId).refreshAllMemberInfoList()
    }
}