/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.api

import androidx.room.*
import com.rongcloud.common.dao.entities.CallRecordEntity
import com.rongcloud.common.dao.model.query.CallRecordModel
import io.reactivex.rxjava3.core.Flowable

/**
 * @author gusd
 * @Date 2021/07/21
 */
@Dao
interface CallRecordDao {


    @Transaction
    @Query(
        """SELECT cr.*,mi.* from CallRecord AS cr LEFT JOIN UserInfo AS mi ON cr.peerId = mi.userId 
        WHERE cr.callerId = :callId"""
    )
    fun queryCallRecordList(callId: String): Flowable<List<CallRecordModel>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCallRecord(vararg callRecordEntity: CallRecordEntity)


}