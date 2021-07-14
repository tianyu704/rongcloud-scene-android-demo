/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.activity.iview

import cn.rongcloud.voiceroomdemo.common.IBaseView

/**
 * @author gusd
 * @Date 2021/06/04
 */
interface ILoginView :IBaseView {
    fun setNextVerificationDuring(time:Long)
    fun onLoginSuccess()
}