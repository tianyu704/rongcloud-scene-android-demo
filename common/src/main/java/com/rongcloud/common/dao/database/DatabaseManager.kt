/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.dao.database

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.rongcloud.common.dao.entities.CallRecordEntity
import com.rongcloud.common.dao.entities.UserInfoEntity
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author gusd
 * @Date 2021/07/21
 */
private const val TAG = "DatabaseManager"

object DatabaseManager {

    private lateinit var context: Application

    private val executor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val instance by lazy {
        Room
            .databaseBuilder(context, AppDatabase::class.java, "VoiceRoomDB")
            .setTransactionExecutor(executor)
            .setQueryExecutor(executor)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun obUserInfoList(): Observable<List<UserInfoEntity>> {
        return instance.memberInfoDao().queryUserInfoList().toObservable()
    }


    fun init(context: Application) {
        this.context = context
    }

    fun insertCallRecordAndMemberInfo(
        callerId: String,
        callerNumber: String?,
        callerName: String?,
        callerPortrait: String?,
        peerId: String,
        peerNumber: String,
        peerName: String?,
        peerPortrait: String?,
        date: Long,
        during: Long,
        callType: Int

    ) {
        doOnDataBaseScheduler {
            instance.runInTransaction {
                try {
                    CallRecordEntity(
                        id = null,
                        callerId = callerId,
                        callerNumber = callerNumber,
                        peerId = peerId,
                        peerNumber = peerNumber,
                        date = date,
                        during = during,
                        callType = callType
                    ).apply {
                        instance.callRecordDao().insertCallRecord(this)
                    }

                    UserInfoEntity(callerId, callerName, callerPortrait, callerNumber).apply {
                        instance.memberInfoDao().addOrUpdate(this)
                    }

                    UserInfoEntity(peerId, peerName, peerPortrait, peerNumber).apply {
                        instance.memberInfoDao().addOrUpdate(this)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "insertCallRecordAndMemberInfo: ", e)
                }
            }
        }

    }

    fun insertCallRecord(
        callerId: String,
        callerNumber: String?,
        peerId: String,
        peerNumber: String,
        date: Long,
        during: Long,
        callType: Int

    ) {
        doOnDataBaseScheduler {
            instance.runInTransaction {
                try {
                    CallRecordEntity(
                        id = null,
                        callerId = callerId,
                        callerNumber = callerNumber,
                        peerId = peerId,
                        peerNumber = peerNumber,
                        date = date,
                        during = during,
                        callType = callType
                    ).apply {
                        instance.callRecordDao().insertCallRecord(this)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "insertCallRecord: ", e)
                }
            }
        }

    }

    fun addOrUpdateUserInfo(userId: String, userName: String?, portrait: String?, number: String?) {
        doOnDataBaseScheduler {
            instance.runInTransaction {
                UserInfoEntity(userId, userName, portrait, number).apply {
                    instance.memberInfoDao().addOrUpdate(this)
                }
            }
        }


    }

    private fun doOnDataBaseScheduler(runnable: () -> Unit) {
        executor.submit(runnable)
    }


}