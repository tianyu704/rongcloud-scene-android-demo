/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import cn.rongcloud.voiceroomdemo.common.BaseLifeCyclePresenter
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/24
 */
class SeatOrderOperationPresenter @Inject constructor(view: IViewPageListView,val roomModel: VoiceRoomModel) :
    BaseLifeCyclePresenter<IViewPageListView>(view) {

    override fun onCreate() {
        super.onCreate()
        roomModel.refreshAllMemberInfoList()
    }
}