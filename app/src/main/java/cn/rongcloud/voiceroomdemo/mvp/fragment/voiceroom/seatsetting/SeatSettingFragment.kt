/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.seatsetting

import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment
import cn.rongcloud.voiceroomdemo.R

/**
 * @author gusd
 * @Date 2021/06/22
 */
class SeatSettingFragment(view: ISeatSettingView) :
    BaseBottomSheetDialogFragment(R.layout.fragment_seat_setting),
    ISeatSettingView by view {

    override fun initView() {

    }
}