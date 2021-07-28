/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.rongcloud.common.base.BaseLifeCyclePresenter
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel
import cn.rongcloud.voiceroomdemo.utils.AudioEffectManager
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/07/05
 */
class MusicSettingPresenter @Inject constructor(
    view: IMusicSettingView,
    private val roomModel: VoiceRoomModel
) :
    BaseLifeCyclePresenter<IMusicSettingView>(view) {

    fun playMusicAtmosphere(name: String) {
        AudioEffectManager.playEffect(name)

    }

    fun getMusicAtmosphereIndexByName(name: String): Int {
        return AudioEffectManager.getMusicAtmosphereIndexByName(name)
    }

    override fun onCreate() {
        super.onCreate()
        roomModel.refreshMusicList()
    }
}