/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.common.AccountStore
import cn.rongcloud.voiceroomdemo.common.loadPortrait
import cn.rongcloud.voiceroomdemo.common.ui
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
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

        iv_member_portrait.loadPortrait(AccountStore.getUserPortrait())
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