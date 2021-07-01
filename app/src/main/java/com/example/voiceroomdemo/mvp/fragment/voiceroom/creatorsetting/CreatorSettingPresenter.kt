/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.VoiceRoomModel
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * @author gusd
 * @Date 2021/06/28
 */
class CreatorSettingPresenter(val view: ICreatorView, roomInfoBean: VoiceRoomBean) :
    BaseLifeCyclePresenter<ICreatorView>(view) {

    private val roomModel: VoiceRoomModel by lazy {
        getVoiceRoomModelByRoomId(roomInfoBean.roomId)
    }

    override fun onCreate() {
        super.onCreate()
        addDisposable(roomModel
            .obSeatInfoChange()
            .filter {
                it.index == 0
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                view.onMuteChange(it.isMute)
            })
    }

    fun leaveSeat() {
        addDisposable(
            roomModel
                .leaveSeat(AccountStore.getUserId()!!)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.fragmentDismiss()
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun muteMic() {
        roomModel.getSeatInfoByUserId(AccountStore.getUserId())?.let {
            addDisposable(
                roomModel
                    .creatorMuteSelf(!it.isMute)
                    .subscribe({

                    }, {
                        view.showError(it.message)
                    })
            )
        }

    }
}