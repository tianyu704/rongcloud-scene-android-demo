/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author gusd
 * @Date 2021/07/28
 */

object ModuleManager {

    @SuppressLint("StaticFieldLeak")
    private lateinit var initService: InitService

     lateinit var applicationContext:Context

    fun bindInitService(application: Application,initService: InitService) {
        this.initService = initService
        applicationContext = application
    }

    fun init(
        onBeforeInit: ((name: String, priority: Int) -> Int)? = null,
        onInit: ((name: String, priority: Int) -> Unit)? = null,
        onInitFinish: (() -> Unit)? = null
    ) {
        initService.init(onBeforeInit, onInit, onInitFinish)
    }



}