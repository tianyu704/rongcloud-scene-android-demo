/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import cn.rongcloud.rtc.api.RCRTCEngine
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.utils.AudioEffectManager

/**
 * @author gusd
 * @Date 2021/07/05
 */
class MusicSettingPresenter(roomId: String, view: IMusicSettingView) :
    BaseLifeCyclePresenter<IMusicSettingView>(view) {

    private val roomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    fun playMusicAtmosphere(name: String) {
        AudioEffectManager.playEffect(name)

    }

    fun getMusicAtmosphereIndexByName(name: String): Int {
        return AudioEffectManager.getMusicAtmosphereIndexByName(name);
    }

    override fun onCreate() {
        super.onCreate()
        roomModel.refreshMusicList()
    }
}