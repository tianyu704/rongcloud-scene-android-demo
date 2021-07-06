/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import com.example.voiceroomdemo.ui.widget.ActionSnackBar
import com.google.android.material.snackbar.BaseTransientBottomBar
import kotlinx.android.synthetic.main.fragment_music_setting.*

/**
 * @author gusd
 * @Date 2021/07/05
 */
class MusicSettingFragment(view: IMusicSettingView) :
    BaseBottomSheetDialogFragment<MusicSettingPresenter, IMusicSettingView>(R.layout.fragment_music_setting),
    IMusicSettingView by view {

    private val actionSnackBar: ActionSnackBar by lazy {
        ActionSnackBar.make(cl_top, R.layout.layout_music_atmosphere).apply {
            getView().setBackgroundColor(resources.getColor(R.color.transparent))
            addCallback(object : BaseTransientBottomBar.BaseCallback<ActionSnackBar>() {
                override fun onDismissed(transientBottomBar: ActionSnackBar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    iv_atmosphere_music.isSelected = false
                }

                override fun onShown(transientBottomBar: ActionSnackBar?) {
                    super.onShown(transientBottomBar)
                    iv_atmosphere_music.isSelected = true
                }
            })
        }
    }

    override fun initPresenter(): MusicSettingPresenter {
        return MusicSettingPresenter(this)
    }

    override fun initView() {
        iv_atmosphere_music.setOnClickListener {
            if (actionSnackBar.isShown) {
                actionSnackBar.dismiss()
            } else {
                actionSnackBar.show()
            }
        }
    }
}