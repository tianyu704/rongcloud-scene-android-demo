/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.database

import android.app.Application
import androidx.room.Room
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author gusd
 * @Date 2021/07/21
 */
object DatabaseManager {

    private lateinit var context: Application

    private val executor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    val instance =
        Room
            .databaseBuilder(context, AppDatabase::class.java, "VoiceRoom")
            .setTransactionExecutor(executor)
            .setQueryExecutor(executor)
            .fallbackToDestructiveMigration()
            .build()

    fun init(context: Application) {
        this.context = context
    }

}