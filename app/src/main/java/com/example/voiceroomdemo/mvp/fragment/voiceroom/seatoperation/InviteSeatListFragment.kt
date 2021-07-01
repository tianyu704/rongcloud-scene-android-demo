/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.seatoperation

import android.view.View
import android.view.ViewGroup
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.loadPortrait
import com.example.voiceroomdemo.mvp.fragment.BaseFragment
import com.example.voiceroomdemo.ui.uimodel.UiMemberModel
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_request_seat_item.view.*

/**
 * @author gusd
 * @Date 2021/06/24
 */
class InviteSeatListFragment(view: IInviteSeatListView, val roomId: String) :
    BaseFragment<InviteSeatListPresenter, IInviteSeatListView>(R.layout.layout_list),
    IInviteSeatListView by view {
    override fun initPresenter(): InviteSeatListPresenter {
        return InviteSeatListPresenter(this, roomId)
    }

    override fun initView() {
        rv_list.adapter = MyAdapter()
    }

    override fun getTitle(): String {
        return "邀请连麦"
    }

    private inner class MyAdapter : BaseListAdapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(parent)
        }

    }

    private inner class MyViewHolder(parent: ViewGroup) : BaseViewHolder(parent) {
        override fun bindView(uiMemberModel: UiMemberModel, itemView: View) {
            with(itemView) {
                iv_user_portrait.loadPortrait(uiMemberModel.portrait)
                tv_member_name.text = uiMemberModel.userName
                tv_operation.text = "邀请"
                tv_operation.isSelected = uiMemberModel.isInvitedInfoSeat
                setOnClickListener {
//                    if(tv_operation.isSelected){
//                        return@setOnClickListener
//                    }
                    presenter.inviteIntoSeat(uiMemberModel)
                }
            }
        }

    }

    override fun refreshData(data: List<UiMemberModel>) {
        super.refreshData(data)
        (rv_list.adapter as? MyAdapter)?.refreshData(data)
    }
}