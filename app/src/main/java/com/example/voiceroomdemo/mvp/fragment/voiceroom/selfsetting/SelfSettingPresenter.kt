/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.selfsetting

import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.ui.uimodel.UiSeatModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/28
 */
class SelfSettingPresenter(val view: ISelfSettingView, var seatInfo: UiSeatModel, roomId: String) :
    BaseLifeCyclePresenter<ISelfSettingView>(view) {

    private var isLeaveSeating = false
    private val roomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    override fun onCreate() {
        super.onCreate()
        addDisposable(roomModel
            .obSeatInfoByIndex(seatInfo.index)
            .subscribe {
                if (seatInfo.userId != AccountStore.getUserId()) {
                    if (isLeaveSeating) {
                        view.showMessage("您已断开连接")
                    }
                    view.fragmentDismiss()
                } else {
                    seatInfo = it
                    view.refreshView(it)
                }
            })

        addDisposable(roomModel
            .obRecordingStatusChange()
            .subscribe {
                view.onRecordStatusChange(it)
            })
    }

    fun muteSelf() {
        if (seatInfo.userId != null) {
            if (seatInfo.isMute) {
                view.showMessage("此座位已被管理员禁麦")
                return
            }
            addDisposable(
                roomModel
                    .setRecordingEnable(!roomModel.recordingStatus)
                    .subscribe({
                        view.showMessage("修改成功")
                    }, {
                        view.showError(it.message)
                    })
            )
        } else {
            view.showError("您已不在该麦位上")
            view.fragmentDismiss()
        }
    }

    fun leaveSeat() {
        isLeaveSeating = true
        addDisposable(
            roomModel
                .leaveSeat(AccountStore.getUserId()!!)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    isLeaveSeating = false
                }
                .subscribe({
                    view.showMessage("您已断开连接")
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }
}