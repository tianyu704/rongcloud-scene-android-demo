/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.ui.uimodel.UiMusicModel

/**
 * @author gusd
 * @Date 2021/07/06
 */
class MusicAddPresenter(val view: IMusicAddView, roomId: String) :
    BaseLifeCyclePresenter<IMusicAddView>(view) {
    private val roomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    override fun onStart() {
        super.onStart()
        addDisposable(
            roomModel
                .obSystemMusicListChange()
                .subscribe({
                    view.showMusicList(arrayListOf<UiMusicModel>().apply {
                        addAll(it)
                        add(UiMusicModel.createLocalAddMusicModel())
                    })
                }, {
                    view.showError(it.message)
                })
        )
    }

    fun addMusic(name: String?, author: String? = "", type: Int = 0, url: String){
        roomModel.addMusic(name?:"", author, type, url)
    }


}
