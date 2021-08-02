/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.content.Context
import android.util.Log
import cn.rongcloud.annotation.AutoInit
import com.rongcloud.common.init.ModuleInit
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/30
 */
private const val TAG = "TestInit1"
@AutoInit
class TestInit1 @Inject constructor():ModuleInit  {
    override fun getPriority(): Int {
        return 100
    }

    override fun getName(context: Context): String {
        return TAG
    }

    override fun onInit(application: Application) {
        Log.d(TAG, "onInit: ")
    }
}