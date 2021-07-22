/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.api

import androidx.room.Dao
import androidx.room.Query

/**
 * @author gusd
 * @Date 2021/07/21
 */
@Dao
interface CallRecordDao {


    @Query("SELECT ")
    fun queryCallRecordList(userId:String)

}