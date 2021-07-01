/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.presenter

import android.content.Context
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine
import com.example.voiceroomdemo.common.AccountStore
import com.example.voiceroomdemo.common.BaseLifeCyclePresenter
import com.example.voiceroomdemo.mvp.activity.iview.IHomeView
import com.example.voiceroomdemo.mvp.model.FileUploadModel
import com.example.voiceroomdemo.net.RetrofitManager
import com.example.voiceroomdemo.net.api.bean.request.UpdateUserInfoRequestBean

/**
 * @author gusd
 * @Date 2021/06/04
 */
private const val TAG = "HomePresenter"

class HomePresenter(val view: IHomeView, val context: Context) :
    BaseLifeCyclePresenter<IHomeView>(view) {

    override fun onDestroy() {
    }

    fun modifyUserInfo(userName: String, selectedPicPath: String?) {
        view.showWaitingDialog()
        if (!selectedPicPath.isNullOrEmpty()) {
            addDisposable(FileUploadModel.imageUpload(selectedPicPath, context).flatMap { url ->
                return@flatMap RetrofitManager.commonService.updateUserInfo(
                    UpdateUserInfoRequestBean(
                        userName,
                        url
                    )
                )
            }.subscribe({ respond ->
                val accountInfo = AccountStore.getAccountInfo()
                    .copy(userName = respond.data?.name, portrait = respond.data?.portrait)
                AccountStore.saveAccountInfo(accountInfo)
                view.modifyInfoSuccess()
                view.hideWaitingDialog()
            }, { t ->
                view.showError(-1, t.message)
            }))
        } else {
            addDisposable(
                RetrofitManager.commonService.updateUserInfo(
                    UpdateUserInfoRequestBean(
                        userName,
                        null
                    )
                ).subscribe({ r ->
                    var accountInfo = AccountStore.getAccountInfo()
                        .copy(userName = r.data?.name)
                    AccountStore.saveAccountInfo(accountInfo)
                    view.modifyInfoSuccess()
                    view.hideWaitingDialog()
                }, { t ->
                    view.showError(-1, t.message)
                })
            )
        }
    }

    fun logout() {
        RCVoiceRoomEngine.getInstance().disConnect()
        AccountStore.logout()
    }
}