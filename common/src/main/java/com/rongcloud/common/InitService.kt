/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author gusd
 * @Date 2021/07/28
 * 各模块的初始化服务
 */
private const val TAG = "InitService"

@Singleton
class InitService @Inject constructor() {
    fun init(application: Application) {
        Log.d(TAG, "init: ")
        ModuleManager.init(application)
    }
}