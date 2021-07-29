/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.content.Context
import cn.rongcloud.annotation.AutoInit
import com.rongcloud.common.init.ModuleInit
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/29
 */
@AutoInit
class CommonInit @Inject constructor() : ModuleInit {
    override fun getPriority(): Int {
        return 0
    }

    override fun getName(context: Context): String {
        return ""
    }

    override fun onInit(application: Application) {
    }
}