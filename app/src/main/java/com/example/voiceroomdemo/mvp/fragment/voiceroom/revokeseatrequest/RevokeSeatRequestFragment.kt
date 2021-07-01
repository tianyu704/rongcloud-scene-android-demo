/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest

import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_cancel_request_seat.*

/**
 * @author gusd
 * @Date 2021/06/29
 */
class RevokeSeatRequestFragment(view: IRevokeSeatView, val roomId: String) :
    BaseBottomSheetDialogFragment<RevokeSeatPresenter, IRevokeSeatView>(
        R.layout.fragment_cancel_request_seat
    ), IRevokeSeatView by view {
    override fun initPresenter(): RevokeSeatPresenter {
        return RevokeSeatPresenter(this, roomId)
    }

    override fun initView() {
        btn_cancel.setOnClickListener {
            dismiss()
        }
        btn_cancel_request.setOnClickListener {
            presenter.cancelRequest()
        }
    }

    override fun fragmentDismiss() {
        dismiss()
    }
}