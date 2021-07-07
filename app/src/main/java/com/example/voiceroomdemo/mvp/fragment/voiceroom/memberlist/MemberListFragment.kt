/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.memberlist

import android.util.Log
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting.IMemberSettingView
import com.example.voiceroomdemo.mvp.fragment.voiceroom.membersetting.MemberSettingFragment
import com.example.voiceroomdemo.net.api.bean.respond.VoiceRoomBean
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import kotlinx.android.synthetic.main.layout_member_list.*

/**
 * @author gusd
 * @Date 2021/06/16
 */
private const val TAG = "MemberListFragment"

class MemberListFragment(
    view: IMemberListView,
    private val roomInfoBean: VoiceRoomBean
) :
    BaseBottomSheetDialogFragment<MemberListPresenter, IMemberListView>(R.layout.layout_member_list),
    IMemberListView by view, IMemberSettingView{


    override fun initPresenter(): MemberListPresenter {
        return MemberListPresenter(this, requireContext(), roomInfoBean)
    }


    override fun initData() {
        presenter.getMemberList()
    }

    override fun initListener() {
        iv_close.setOnClickListener {
           dismiss()
        }
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun initView() {
        rv_member_list.adapter = MemberListAdapter {
            Log.d(TAG, "item onClick: $it")
            if (AccountStore.getUserId() == it.userId) {
                // 点击自己不做任何反应
                return@MemberListAdapter
            }
            MemberSettingFragment(this,roomInfoBean,it).show(childFragmentManager)

        }

    }

    override fun showMemberList(data: List<UiMemberModel>?) {
        Log.d(TAG, "showMemberList: $data")
        rv_member_list.post {
            data?.let {
                (rv_member_list.adapter as? MemberListAdapter)?.refreshData(data)
            }
        }
    }

}