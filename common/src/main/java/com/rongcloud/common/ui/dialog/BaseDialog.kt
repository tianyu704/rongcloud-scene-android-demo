/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.rongcloud.common.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.rongcloud.common.R

/**
 * @author gusd
 * @Date 2021/06/08
 */
abstract class BaseDialog(
    context: Context,
    private val layoutId: Int,
    private val touchOutSideDismiss: Boolean
) : Dialog(context, R.style.CustomDialog) {

    protected lateinit var rootView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(touchOutSideDismiss)
        rootView = LayoutInflater.from(context).inflate(layoutId, null, false)
        setContentView(rootView)
        initView()
        initListener()
    }


    abstract fun initListener()

    abstract fun initView()

}