/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.creatorsetting

import cn.rongcloud.annotation.HiltBinding
import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import com.rongcloud.common.extension.loadPortrait
import com.rongcloud.common.extension.ui
import com.rongcloud.common.utils.AccountStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragmeng_creator_setting.*
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/28
 */
@HiltBinding(value = ICreatorView::class)
@AndroidEntryPoint
class CreatorSettingFragment(view: ICreatorView, private val roomInfoBean: VoiceRoomBean) :
    BaseBottomSheetDialogFragment(R.layout.fragmeng_creator_setting),
    ICreatorView by view {

    @Inject
    lateinit var presenter: CreatorSettingPresenter


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