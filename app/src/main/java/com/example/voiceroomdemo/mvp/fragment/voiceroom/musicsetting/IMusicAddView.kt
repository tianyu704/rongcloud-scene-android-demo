/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.example.voiceroomdemo.common.IBaseView
import com.example.voiceroomdemo.ui.uimodel.UiMusicModel

/**
 * @author gusd
 * @Date 2021/07/06
 */
interface IMusicAddView:IBaseView {
    fun showMusicList(list: List<UiMusicModel>) {}
}