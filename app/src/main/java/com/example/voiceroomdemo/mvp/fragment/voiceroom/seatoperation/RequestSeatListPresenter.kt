/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/24
 */
class RequestSeatListPresenter(val view: IRequestSeatListView, roomId: String) :
    BaseLifeCyclePresenter<IRequestSeatListView>(view) {
    private val roomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onResume() {
        super.onResume()
        addDisposable(
            roomModel
                .obRequestSeatListChange()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.refreshData(it)
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun acceptRequest(uiMemberModel: UiMemberModel) {
        addDisposable(
            roomModel
                .acceptRequest(uiMemberModel.userId)
                .subscribe({

                }, {
                    view.showError(it.message)
                })
        )
    }
}