/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.ui
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import kotlinx.android.synthetic.main.fragmeng_creator_setting.*

/**
 * @author gusd
 * @Date 2021/06/28
 */
class CreatorSettingFragment(view: ICreatorView, private val roomInfoBean: VoiceRoomBean) :
    BaseBottomSheetDialogFragment<CreatorSettingPresenter, ICreatorView>(R.layout.fragmeng_creator_setting),
    ICreatorView by view {
    override fun initPresenter(): CreatorSettingPresenter {
        return CreatorSettingPresenter(this, roomInfoBean)
    }

    override fun initView() {
        btn_out_of_seat.setOnClickListener {
            presenter.leaveSeat()
        }
        btn_mute_self.setOnClickListener {
            presenter.muteMic()
        }
        tv_member_name.text = roomInfoBean.createUser?.userName
    }

    override fun fragmentDismiss() {
        super.fragmentDismiss()
        dismiss()
    }

    override fun onMuteChange(isMute: Boolean) {
        super.onMuteChange(isMute)
        ui {
            if (isMute) {
                btn_mute_self.text = "打开麦克风"
            } else {
                btn_mute_self.text = "关闭麦克风"
            }
        }
    }
}