/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import com.rongcloud.common.base.IBaseView

/**
 * @author gusd
 * @Date 2021/06/28
 */
interface ICreatorView: IBaseView {
    fun fragmentDismiss(){}
    fun onMuteChange(isMute: Boolean) {

    }
}