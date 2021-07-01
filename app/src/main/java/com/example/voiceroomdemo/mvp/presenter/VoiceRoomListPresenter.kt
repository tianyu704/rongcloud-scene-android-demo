/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.presenter

import android.content.Context
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.activity.VoiceRoomActivity
import com.example.voiceroomdemo.mvp.activity.iview.IVoiceRoomListView
import com.example.voiceroomdemo.mvp.model.EMPTY_ROOM_INFO
import com.example.voiceroomdemo.mvp.model.VoiceRoomListModel
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/09
 */
class VoiceRoomListPresenter(
    val view: IVoiceRoomListView
) :
    BaseLifeCyclePresenter<IVoiceRoomListView>(view) {

    private val voiceRoomListMode: VoiceRoomListModel by lazy {
        VoiceRoomListModel
    }

    override fun onCreate() {
        super.onCreate()
        initObDataChange()
    }

    private fun initObDataChange() {
        addDisposable(
            voiceRoomListMode
                .obVoiceRoomList()
                .subscribe { bean ->
                    view.onDataChange(bean)
                }
        )

        addDisposable(voiceRoomListMode
            .obVoiceRoomErrorEvent()
            .subscribe {
                view.onLoadError(it)
            })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        voiceRoomListMode.refreshDataList()
    }

    fun refreshData() {
        voiceRoomListMode.refreshDataList()
    }

    fun loadMore() {
        voiceRoomListMode.loadMoreData()
    }

    fun gotoVoiceRoomActivity(context: Context, roomId: String) {
        view.showWaitingDialog()
        voiceRoomListMode
            .queryRoomInfoFromServer(roomId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.hideWaitingDialog() }
            .doFinally {
                view.hideWaitingDialog()
            }
            .subscribe({ info ->
                if (info.room == null || info.room == EMPTY_ROOM_INFO) {
                    view.showError("房间不存在")
                } else {
                    if (info.room.isPrivate == 1 && info.room.createUser?.userId != AccountStore.getUserId()) {
                        view.showInputPasswordDialog(info.room)
                    } else {
                        turnToRoom(context, info.room)
                    }
                }
            }, { t ->
                view.showError(t.message)
            })

    }

    fun turnToRoom(context: Context, info: VoiceRoomBean) {
        info.createUser?.let {
            VoiceRoomActivity.startActivity(context, info.roomId, info.createUser.userId)
        } ?: view.showError("房间数据错误")
    }
}