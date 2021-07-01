/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.seatsetting

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment

/**
 * @author gusd
 * @Date 2021/06/22
 */
class SeatSettingFragment(view: ISeatSettingView) :
    BaseBottomSheetDialogFragment<SeatSettingPresenter, ISeatSettingView>(R.layout.fragment_seat_setting),
    ISeatSettingView by view {
    override fun initPresenter(): SeatSettingPresenter {
        return SeatSettingPresenter(this)
    }

    override fun initView() {

    }
}