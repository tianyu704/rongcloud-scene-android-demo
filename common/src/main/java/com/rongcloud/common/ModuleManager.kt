/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.content.Context

/**
 * @author gusd
 * @Date 2021/07/28
 */
object ModuleManager {

    lateinit var applicationContext: Application

    fun init(application: Application) {
        applicationContext = application
    }


}