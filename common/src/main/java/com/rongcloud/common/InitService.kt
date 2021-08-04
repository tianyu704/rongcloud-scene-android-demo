/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common

import android.app.Application
import android.content.Context
import com.rongcloud.common.init.ModuleInit
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author gusd
 * @Date 2021/07/28
 * 各模块的初始化服务
 */
private const val TAG = "InitService"

@Singleton
class InitService @Inject constructor() {

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Named("autoInit")
    @Inject
    lateinit var initModuleList:Lazy<ArrayList<ModuleInit>>

    fun init(
        onBeforeInit: ((name: String, priority: Int) -> Int)?,
        onModuleInitFinish: ((name: String, priority: Int) -> Unit)?,
        onAllInitFinish: (() -> Unit)?
    ) {
        val initList = initModuleList.get()
        val needInitModule = arrayListOf<Pair<Int, ModuleInit>>()
        initList.sortBy { it.getPriority() }
        initList.forEach {
            val realPriority =
                onBeforeInit?.invoke(it.getName(context), it.getPriority()) ?: it.getPriority()
            if (realPriority >= 0) {
                needInitModule.add(Pair(realPriority, it))
            }
        }
        needInitModule.sortedBy { it.first }.forEach {
            it.second.onInit(context as Application)
            onModuleInitFinish?.invoke(it.second.getName(context), it.first)
        }
        onAllInitFinish?.invoke()
    }
}