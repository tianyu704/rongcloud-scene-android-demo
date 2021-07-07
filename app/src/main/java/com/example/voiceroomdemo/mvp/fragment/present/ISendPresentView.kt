/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.present

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.mvp.model.Present
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel

/**
 * @author baicq
 * @Date 2021/06/28
 */
interface ISendPresentView:IBaseView {
    fun fragmentDismiss(){}
    fun onMemberModify(members:List<UiMemberModel>) {
    }

    fun onPresentInited(members:List<Present>) {
    }

    fun onEnableSend(enable:Boolean){

    }
}