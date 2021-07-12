/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting

import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.VoiceRoomModel
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/21
 */
class MemberSettingPresenter(
    private val view: IMemberSettingView,
    private val roomInfoBean: VoiceRoomBean,
    private var member: UiMemberModel
) :
    BaseLifeCyclePresenter<IMemberSettingView>(view) {
    private val roomModel: VoiceRoomModel by lazy {
        getVoiceRoomModelByRoomId(roomInfoBean.roomId)
    }

    override fun onCreate() {
        super.onCreate()
        addDisposable(
            roomModel
                .obMemberInfoChange()
                .filter {
                    member.userId == it.userId
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    this.member = it
                    refreshView()
                }
        )
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }


    private fun refreshView() {
        view.loginUserIsCreator(
            roomInfoBean.createUser?.userId == AccountStore.getUserId(),
            roomModel.isAdmin(AccountStore.getUserId()!!)
        )
        view.thisUserIsOnSeat(member.seatIndex,roomModel.isAdmin(AccountStore.getUserId()!!))
        view.thisUserIsAdmin(member.isAdmin)
        view.thisUserIsMute(roomModel.getSeatInfoByUserId(member.userId)?.isMute ?: false)
    }

    fun kickSeat() {
        addDisposable(
            roomModel
                .kickSeat(member.userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showMessage("发送下麦通知成功")
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun closeSeat() {
        addDisposable(
            roomModel
                .setSeatLockByUserId(member.userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun muteSeat() {
        roomModel.getSeatInfoByUserId(member.userId)?.isMute?.let { isMute ->
            addDisposable(
                roomModel
                    .setSeatMuteByUserId(member.userId, !isMute)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.fragmentDismiss()
                    }, {
                        view.showError(it.message)
                    })
            )
        }

    }

    fun kickRoom() {
        addDisposable(
            roomModel
                .kickRoom(member.userId).subscribe({
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun inviteEnterSeat() {
        addDisposable(
            roomModel
                .invitedIntoSeat(member.userId)
                .subscribe({
                    view.showMessage("发送上麦通知成功")
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun toggleAdmin() {
        addDisposable(
            roomModel
                .setAdmin(member.userId, !member.isAdmin)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isSuccess ->
                }, { t ->
                    view.showError(t.message)
                })
        )
    }
}