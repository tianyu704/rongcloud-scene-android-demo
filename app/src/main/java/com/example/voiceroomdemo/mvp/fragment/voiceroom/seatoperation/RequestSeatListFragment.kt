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
class RequestSeatListFragment(view: IRequestSeatListView, val roomId: String) :
    BaseFragment<RequestSeatListPresenter, IRequestSeatListView>(R.layout.layout_list),
    IRequestSeatListView by view {

    override fun initPresenter(): RequestSeatListPresenter {
        return RequestSeatListPresenter(this, roomId)
    }

    override fun initView() {
        rv_list.adapter = MyAdapter()

    }

    override fun getTitle(): String {
        return "申请连麦"
    }

    override fun refreshData(list: List<UiMemberModel>) {
        super.refreshData(list)
        (rv_list.adapter as? MyAdapter)?.refreshData(list)
    }

    private inner class MyAdapter() : BaseListAdapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(parent)
        }

    }

    private inner class MyViewHolder(parent: ViewGroup) : BaseViewHolder(parent) {
        override fun bindView(uiMemberModel: UiMemberModel, itemView: View) {
            with(itemView) {
                iv_user_portrait.loadPortrait(uiMemberModel.portrait)
                tv_member_name.text = uiMemberModel.userName
                tv_operation.text = "接受"
                setOnClickListener {
                    presenter.acceptRequest(uiMemberModel)
                }
            }
        }

    }
}



