/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.common

/**
 * @author gusd
 * @Date 2021/06/04
 */
abstract class BasePresenter<T:IBaseView> {

    abstract fun onCreate()
    abstract fun onDestroy()
}