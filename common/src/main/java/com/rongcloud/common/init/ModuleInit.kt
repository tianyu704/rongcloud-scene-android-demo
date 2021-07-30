/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.init

import android.app.Application
import android.content.Context

/**
 * @author gusd
 * @Date 2021/07/28
 */
interface ModuleInit {
    fun getPriority(): Int
    fun getName(context: Context): String
    fun onInit(application: Application)
}