/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.utils

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager


import android.view.KeyEvent

import android.view.KeyCharacterMap

import android.view.ViewConfiguration

import android.app.Activity
import android.graphics.Point


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
     * 获取屏幕高度,包括状态栏
     */
    fun getFullScreenHeight(context: Context): Int {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm)
        } else {
            display.getMetrics(dm)
        }
        return dm.heightPixels
    }
    /**
     * 获取虚拟导航栏的高度
     */
    fun getNavigationBarHeight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            val resources = activity.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val height = resources.getDimensionPixelSize(resourceId)
            //超出系统默认的导航栏高度以上，则认为存在虚拟导航
            if (realSize.y - size.y > height - 10) {
                height
            } else 0
        } else {
            val menu = ViewConfiguration.get(activity).hasPermanentMenuKey()
            val back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            if (menu || back) {
                0
            } else {
                val resources = activity.resources
                val resourceId =
                    resources.getIdentifier("navigation_bar_height", "dimen", "android")
                resources.getDimensionPixelSize(resourceId)
            }
        }
    }

    /**
     * 获取屏幕高度,不包括状态栏
     */
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

    /**
     * 获取屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    /**
     * 获取屏幕中位置
     */
    fun getLocation(view: View): IntArray {
        val location = IntArray(2)
        if (Build.VERSION.SDK_INT >= 24) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            location[0] = rect.left
            location[1] = rect.top
        } else {
            view.getLocationOnScreen(location)
        }
        Log.d("getLocation", "(x,y)=(${location[0]},${location[1]})")
        return location
    }
}