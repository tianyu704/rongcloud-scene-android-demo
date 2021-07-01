/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel

/**
 * @author gusd
 * @Date 2021/06/24
 */
interface IInviteSeatListView:IBaseView {
    fun refreshData(data: List<UiMemberModel>) {

    }
}