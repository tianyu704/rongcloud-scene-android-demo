/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rongcloud.common.dao.api.CallRecordDao
import com.rongcloud.common.dao.entities.CallRecordEntity

/**
 * @author gusd
 * @Date 2021/07/21
 */
@Database(entities = arrayOf(CallRecordEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun callRecordDao(): CallRecordDao
}