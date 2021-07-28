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
        """SELECT cr.id,cr.callerNumber,cr.callerId,cr.peerId,cr.peerNumber,cr.date,cr.during,cr.callType,ui.userName,ui.number,ui.portrait 
            From CallRecord AS cr LEFT JOIN UserInfo AS ui ON cr.peerId = ui.userId 
        WHERE cr.callerId = :callId  GROUP BY cr.callerNumber ORDER BY cr.date DESC """
    )
    fun queryCallRecordList(callId: String): Flowable<List<CallRecordModel>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCallRecord(vararg callRecordEntity: CallRecordEntity)


}