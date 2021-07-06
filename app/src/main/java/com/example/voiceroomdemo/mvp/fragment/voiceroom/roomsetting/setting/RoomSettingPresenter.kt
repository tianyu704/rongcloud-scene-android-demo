/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting

import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting.functionmodel.*
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean

/**
 * @author gusd
 * @Date 2021/06/22
 */

class RoomSettingPresenter(view: IRoomSettingView, private val roomInfoBean: VoiceRoomBean) :
    BaseLifeCyclePresenter<IRoomSettingView>(view) {

    private var functionList: ArrayList<BaseFunctionModel> = arrayListOf()

    init {
        functionList.apply {
            add(LockRoomFunction(roomInfoBean.roomId, view))
            add(RoomNameFunction(roomInfoBean.roomId, view))
            add(BackgroundFunction(view))
            add(EnterSeatModelFunction(roomInfoBean.roomId, view))
            add(MuteAllSeatFunction(roomInfoBean.roomId, view))
            add(LockAllSeatFunction(roomInfoBean.roomId, view))
            add(MuteAllRemoteFunction(roomInfoBean.roomId,view))
            add(ChangeSeatFunction(roomInfoBean.roomId,view))
//            add(MusicFunction(roomInfoBean.roomId,view))
        }
    }


    fun getButtons(): List<BaseFunctionModel> {
        return functionList
    }

    override fun onCreate() {
        super.onCreate()
        functionList.forEach {
            it.onCreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        functionList.forEach {
            it.onDestroy()
        }
    }
}