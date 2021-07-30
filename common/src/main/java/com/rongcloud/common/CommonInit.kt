/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.content.Context
import android.util.Log
import cn.rongcloud.annotation.AutoInit
import com.rongcloud.common.init.ModuleInit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/29
 */
private const val TAG = "CommonInit"

@AutoInit
class CommonInit @Inject constructor() : ModuleInit {

    /**
     * 可通过该种方式获取 ApplicationContext
     */
    @Inject
    @ApplicationContext
    lateinit var context: Context

    override fun getPriority(): Int {
        return 0
    }

    override fun getName(context: Context): String {
        return this.javaClass.name
    }

    override fun onInit(application: Application) {
        Log.d(TAG, "onInit: ")
    }
}