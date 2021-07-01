/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.activity.iview

import com.example.voiceroomdemo.common.IBaseView

/**
 * @author gusd
 * @Date 2021/06/04
 */
interface ILoginView :IBaseView {
    fun setNextVerificationDuring(time:Long)
    fun onLoginSuccess()
}