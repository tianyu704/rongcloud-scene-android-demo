/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment

/**
 * @author gusd
 * @Date 2021/07/05
 */
class MusicSettingFragment(view: IMusicSettingView) :
    BaseBottomSheetDialogFragment<MusicSettingPresenter, IMusicSettingView>(R.layout.fragment_music_setting),
    IMusicSettingView by view {
    override fun initPresenter(): MusicSettingPresenter {
        return MusicSettingPresenter(this)
    }

    override fun initView() {

    }
}