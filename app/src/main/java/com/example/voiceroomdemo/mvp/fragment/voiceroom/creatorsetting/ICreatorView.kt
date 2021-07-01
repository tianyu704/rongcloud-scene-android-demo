/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import com.example.voiceroomdemo.common.IBaseView

/**
 * @author gusd
 * @Date 2021/06/28
 */
interface ICreatorView:IBaseView {
    fun fragmentDismiss(){}
    fun onMuteChange(isMute: Boolean) {

    }
}