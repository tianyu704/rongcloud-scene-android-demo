/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.emptyseatsetting

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.ui.uimodel.UiSeatModel

/**
 * @author gusd
 * @Date 2021/06/28
 */
interface IEmptySeatView:IBaseView {
    fun refreshView(uiSeatModel: UiSeatModel){}
    fun showInviteUserView(){}
}