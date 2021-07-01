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
class InviteSeatListPresenter(val view: IInviteSeatListView, roomId: String) :
    BaseLifeCyclePresenter<IInviteSeatListView>(view) {


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
                .obInviteSeatListChange()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.refreshData(it)
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun inviteIntoSeat(uiMemberModel: UiMemberModel) {
        addDisposable(
            roomModel
                .invitedIntoSeat(uiMemberModel.userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    view.showError(it.message)
                })
        )
    }
}