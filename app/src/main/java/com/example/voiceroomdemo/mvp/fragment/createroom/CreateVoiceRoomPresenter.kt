/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.createroom

import android.content.Context
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.FileUploadModel
import com.example.voiceroomdemo.net.RetrofitManager
import com.example.voiceroomdemo.net.api.ApiConstant
import com.example.voiceroomdemo.net.api.bean.request.CreateRoomRequestBean
import com.example.voiceroomdemo.net.api.bean.request.Kv

/**
 * @author gusd
 * @Date 2021/06/15
 */
class CreateVoiceRoomPresenter(val view: ICreateVoiceRoomView, val context: Context) :
    BaseLifeCyclePresenter<ICreateVoiceRoomView>(view) {

    fun createVoiceRoom(
        roomCover: String,
        roomName: String,
        roomBackground: String,
        isPrivate: Boolean,
        roomPassword: String?
    ) {
        view.showWaitingDialog()
        val intPrivate = if (isPrivate) 1 else 0
        val password = if (isPrivate) roomPassword else ""
        val rcRoomInfo: RCVoiceRoomInfo = RCVoiceRoomInfo().apply {
            this.roomName = roomName
            this.isFreeEnterSeat = false
            this.seatCount = 9
        }
        val kvList = ArrayList<Kv>().apply {
            add(Kv("RCRoomInfoKey", rcRoomInfo.toJson()))
        }
        if (!roomCover.isNullOrEmpty()) {
            addDisposable(FileUploadModel
                .imageUpload(roomCover, context)
                .flatMap {
                    return@flatMap RetrofitManager
                        .commonService
                        .createVoiceRoom(
                            CreateRoomRequestBean(
                                intPrivate,
                                kvList,
                                roomName,
                                password,
                                "${ApiConstant.BASE_URL}$it",
                                roomBackground
                            )
                        )
                }.subscribe({ respond ->
                    view.hideWaitingDialog()
                    when (respond.code) {
                        10000 -> {
                            view.onCreateRoomSuccess(respond.data)
                        }
                        30016 -> {
                            view.onCreateRoomExist(respond.data)
                        }
                        else -> {
                            view.showError(respond.code ?: -1, respond.msg)
                        }
                    }
                }, { t ->
                    view.hideWaitingDialog()
                    view.showError(-1, t.message)
                })
            )
        } else {
            addDisposable(
                RetrofitManager
                    .commonService
                    .createVoiceRoom(
                        CreateRoomRequestBean(
                            isPrivate = intPrivate,
                            kv = kvList,
                            name = roomName,
                            password = password,
                            backgroundUrl = roomBackground
                        )
                    ).subscribe({ respond ->
                        view.hideWaitingDialog()
                        when (respond.code) {
                            10000 -> {
                                view.onCreateRoomSuccess(respond.data)
                            }
                            30016 -> {
                                view.onCreateRoomExist(respond.data)
                            }
                            else -> {
                                view.showError(respond.code ?: -1, respond.msg)
                            }
                        }
                    }, { t ->
                        view.hideWaitingDialog()
                        view.showError(-1, t.message)
                    })
            )
        }
    }


}