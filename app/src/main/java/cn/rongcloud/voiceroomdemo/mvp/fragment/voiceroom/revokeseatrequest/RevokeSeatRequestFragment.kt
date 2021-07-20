/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.revokeseatrequest

import cn.rongcloud.voiceroomdemo.R
import cn.rongcloud.voiceroomdemo.mvp.fragment.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_cancel_request_seat.*
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/29
 */
class RevokeSeatRequestFragment(view: IRevokeSeatView) :
    BaseBottomSheetDialogFragment<RevokeSeatPresenter, IRevokeSeatView>(
        R.layout.fragment_cancel_request_seat
    ), IRevokeSeatView by view {

    @Inject
    lateinit var presenter:RevokeSeatPresenter

    override fun initPresenter(): RevokeSeatPresenter {
        return presenter
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