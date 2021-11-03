/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.presenter

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import cn.rong.combusis.api.VRApi
import cn.rong.combusis.common.utils.RealPathFromUriUtils
import cn.rongcloud.voiceroom.model.FileModel
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.ISettingView
import cn.rongcloud.voiceroomdemo.net.CommonNetManager
import cn.rongcloud.voiceroomdemo.net.api.bean.request.UpdateUserInfoRequestBean
import com.basis.net.oklib.OkApi
import com.basis.net.oklib.WrapperCallBack
import com.basis.net.oklib.wrapper.Wrapper
import com.rongcloud.common.base.BaseLifeCyclePresenter
import com.rongcloud.common.utils.AccountStore
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/04
 */
private const val TAG = "HomePresenter"

@ActivityScoped
class SettingPresenter @Inject constructor(
    val view: ISettingView,
    @ActivityContext val context: Context,
    activity: AppCompatActivity
) :
    BaseLifeCyclePresenter(activity) {

    override fun onDestroy() {
    }

    fun modifyUserInfo(userName: String, selectedPicPath: Uri?) {
        view.showWaitingDialog()
        if (selectedPicPath != null) {
            addDisposable(
                FileModel.imageUpload(
                    RealPathFromUriUtils.getRealPathFromUri(
                        context,
                        selectedPicPath
                    ), context
                ).flatMap { url ->
                    return@flatMap CommonNetManager
                        .commonService
                        .updateUserInfo(
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
                })
            )
        } else {
            addDisposable(
                CommonNetManager
                    .commonService
                    .updateUserInfo(
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

    fun unregister() {
        OkApi.post(VRApi.RESIGN, null, object : WrapperCallBack() {
            override fun onResult(result: Wrapper?) {
                if (result?.ok() == true) {
                    view.showMessage("注销成功")
                    logout()
                }
            }
        })
    }

    fun logout() {
        AccountStore.logout()
    }
}