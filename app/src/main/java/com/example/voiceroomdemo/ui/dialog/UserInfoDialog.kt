/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.ui.dialog

import android.content.Context
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.*
import kotlinx.android.synthetic.main.layout_user_info_popup_window.*

/**
 * @author gusd
 * @Date 2021/06/08
 */
class UserInfoDialog(
    context: Context,
    private val logoutBlock: (() -> Unit)? = null,
    private val saveBlock: ((userName: String, portrait: String?) -> Unit)? = null,
    private val showPictureSelectBlock: (() -> Unit)? = null
) : BaseDialog(
    context, R.layout.layout_user_info_popup_window,
    false
) {

    private var selectedPicPath: String? = null

    override fun initListener() {

    }

    override fun initView() {
        iv_portrait.loadPortrait(AccountStore.getUserPortrait() ?: "")
        AccountStore.getUserName()?.let {
            et_user_name.setText(it)
        }
        iv_close.setOnClickListener {
            dismiss()
        }

        tv_save_user_info.setOnClickListener {
            if (et_user_name.text.isNullOrBlank()) {
                context.showToast(getString(R.string.username_can_not_be_empty))
                return@setOnClickListener
            }
            saveBlock?.invoke(et_user_name.text.toString(), selectedPicPath)
        }

        iv_portrait.setOnClickListener {
            showPictureSelectBlock?.invoke()
        }

        tv_logout.setOnClickListener {
            logoutBlock?.invoke()
        }

    }

    fun setUserPortrait(picturePath: String) {
        selectedPicPath = picturePath
        iv_portrait.loadLocalPortrait(picturePath)
    }
}