/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.utils

import android.app.Activity
import android.content.Context

/**
 * @author gusd
 * @Date 2021/06/09
 */
object UiUtils {
    fun dp2Px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density //当前屏幕密度因子
        return (dp * scale + 0.5f).toInt()
    }

    fun px2Dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }

    /**
     * 获取屏幕高度
     */
    fun getScreenHeight(activity:Activity):Int{
        return activity.windowManager.defaultDisplay.height
    }

    /**
     * 获取屏幕宽度
     */
    fun getScreenWidth(activity:Activity):Int{
        return activity.windowManager.defaultDisplay.width
    }
}