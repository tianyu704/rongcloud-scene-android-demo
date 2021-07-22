/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.api

import androidx.room.Dao
import androidx.room.Query
import com.rongcloud.common.dao.model.query.CallRecordModel
import io.reactivex.rxjava3.core.Flowable

/**
 * @author gusd
 * @Date 2021/07/21
 */
@Dao
interface CallRecordDao {


    @Query(
        """SELECT cr.* from CallRecord AS cr LEFT JOIN MemberInfo AS mi ON cr.peerId = mi.userId 
        WHERE cr.callerId = :callId"""
    )
    fun queryCallRecordList(callId: String): Flowable<List<CallRecordModel>>



}