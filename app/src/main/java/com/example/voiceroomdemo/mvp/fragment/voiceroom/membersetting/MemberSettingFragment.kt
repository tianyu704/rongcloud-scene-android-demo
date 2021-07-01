/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting

import androidx.core.view.isVisible
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.loadPortrait
import com.example.voiceroomdemo.common.ui
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import kotlinx.android.synthetic.main.layout_member_setting.*

/**
 * @author gusd
 * @Date 2021/06/21
 */
class MemberSettingFragment(
    val view: IMemberSettingView,
    private val roomInfoBean: VoiceRoomBean,
    private val member: UiMemberModel
) :
    BaseBottomSheetDialogFragment<MemberSettingPresenter, IMemberSettingView>(R.layout.layout_member_setting),
    IMemberSettingView by view {
    override fun initPresenter(): MemberSettingPresenter {
        return MemberSettingPresenter(this, roomInfoBean, member)
    }

    override fun initView() {
        if (roomInfoBean.createUser?.userId == AccountStore.getUserId()) {
            rl_setting_admin.isVisible = true
            cl_member_setting.isVisible = true
        }


        iv_member_portrait.loadPortrait(member.portrait)
        tv_member_name.text = member.userName

        rl_setting_admin.setOnClickListener {
            presenter.toggleAdmin()
        }

        ll_invited_seat.setOnClickListener {
            presenter.inviteEnterSeat()
        }
        ll_kick_room.setOnClickListener {
            presenter.kickRoom()
        }
        ll_kick_seat.setOnClickListener {
            presenter.kickSeat()
        }
        ll_mute_seat.setOnClickListener {
            presenter.muteSeat()
        }
        ll_close_seat.setOnClickListener {
            presenter.closeSeat()
        }
    }

    override fun initListener() {
        super.initListener()
        btn_send_gift.setOnClickListener {
            view.showError("暂未开放")
        }
        btn_send_message.setOnClickListener {
            view.showError("暂未开放")
        }


    }


    override fun loginUserIsCreator(isCreatorUser: Boolean, isAdmin: Boolean) {
        ui {
            when {
                isCreatorUser -> {
                    rl_setting_admin.isVisible = true
                    cl_member_setting.isVisible = true
                }
                isAdmin -> {
                    rl_setting_admin.isVisible = false
                    cl_member_setting.isVisible = true
                }
                else -> {
                    rl_setting_admin.isVisible = false
                    cl_member_setting.isVisible = false
                }
            }
        }
    }


    override fun thisUserIsOnSeat(seatIndex: Int) {
        ui {
            if (seatIndex > -1) {
                ll_kick_seat.isVisible = true
                ll_close_seat.isVisible = true
                ll_mute_seat.isVisible = true
                ll_kick_room.isVisible = true
                ll_invited_seat.isVisible = false
                tv_seat_position.isVisible = true
                tv_seat_position.text = "$seatIndex 号麦位"

            } else {
                ll_kick_seat.isVisible = false
                ll_close_seat.isVisible = false
                ll_mute_seat.isVisible = false
                ll_kick_room.isVisible = true
                ll_invited_seat.isVisible = true
                tv_seat_position.isVisible = false
            }
        }
    }

    override fun thisUserIsAdmin(isAdmin: Boolean) {
        if (isAdmin) {
            tv_setting_admin.text = "撤回管理"
            tv_setting_admin.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_member_setting_is_admin,
                0,
                0,
                0
            )
        } else {
            tv_setting_admin.text = "设为管理"
            tv_setting_admin.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_member_setting_not_admin,
                0,
                0,
                0
            )
        }
    }

    override fun thisUserIsMute(isMute: Boolean) {
        if (isMute) {
            iv_mute_seat.setImageResource(R.drawable.ic_room_setting_unmute_all)
            tv_mute_seat.text = "座位开麦"

        } else {
            iv_mute_seat.setImageResource(R.drawable.ic_member_setting_mute_seat)
            tv_mute_seat.text = "座位闭麦"
        }
    }

    override fun fragmentDismiss() {
        dismiss()
    }
}