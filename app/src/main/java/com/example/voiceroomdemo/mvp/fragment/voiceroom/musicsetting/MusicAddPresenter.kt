/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import android.util.Log
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.model.FileModel
import com.example.voiceroomdemo.mvp.model.getVoiceRoomModelByRoomId
import com.example.voiceroomdemo.ui.uimodel.UiMusicModel

/**
 * @author gusd
 * @Date 2021/07/06
 */
private const val TAG = "MusicAddPresenter"

class MusicAddPresenter(val view: IMusicAddView, roomId: String) :
    BaseLifeCyclePresenter<IMusicAddView>(view) {
    private val roomModel by lazy {
        getVoiceRoomModelByRoomId(roomId)
    }

    private val fileModel by lazy {
        FileModel
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

    fun addMusic(name: String?, author: String? = "", type: Int = 0, url: String) {
        view.showWaitingDialog()
        addDisposable(roomModel
            .addMusic(name ?: "", author, type, url)
            .subscribe {
                addDisposable(fileModel
                    .checkOrDownLoadMusic(url)
                    { total, progress ->
                        Log.d(TAG, "addMusic:total = $total,progress = $progress ")
                    }.subscribe({
                        Log.d(TAG, "addMusic: onComplete")
                        view.hideWaitingDialog()
                    }, {
                        view.hideWaitingDialog()
                    })
                )
            })
    }


}
