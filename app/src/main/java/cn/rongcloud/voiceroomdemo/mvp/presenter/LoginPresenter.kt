/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.presenter

import androidx.appcompat.app.AppCompatActivity
import cn.rongcloud.voiceroomdemo.mvp.activity.iview.ILoginView
import cn.rongcloud.voiceroomdemo.mvp.model.LoginModel
import com.kit.cache.GsonUtil
import com.rongcloud.common.base.BaseLifeCyclePresenter
import com.rongcloud.common.net.ApiConstant
import com.rongcloud.common.utils.AccountStore
import dagger.hilt.android.scopes.ActivityScoped
import io.rong.imkit.RongIM
import io.rong.imlib.RongIMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/04
 */
@ActivityScoped
class LoginPresenter @Inject constructor(
    val view: ILoginView,
    private val loginModel: LoginModel,
    activity: AppCompatActivity
) :
    BaseLifeCyclePresenter(activity) {

    override fun onCreate() {
    }

    override fun onDestroy() {
    }

    fun getVerificationCode(phoneNumber: String) {
        view.showWaitingDialog()
        GlobalScope.launch(Dispatchers.IO) {
            addDisposable(
                loginModel
                    .getVerificationCode(phoneNumber)
                    .doFinally {
                        view.hideWaitingDialog()
                    }
                    .subscribe({ bean ->
                        view.apply {
                            if (bean.code == ApiConstant.REQUEST_SUCCESS_CODE) {
                                setNextVerificationDuring(60 * 1000L)
                            } else {
                                view.showError(bean.code, bean.msg)
                            }
                        }

                    }, { throwable ->
                        view.showError(-1, throwable.message)
                    })
            )
        }

    }

    fun login(phoneNumber: String, verifyCode: String) {
        view.showWaitingDialog()
        GlobalScope.launch(Dispatchers.IO) {
            addDisposable(
                loginModel
                    .login(phoneNumber, verifyCode)
                    .doFinally {
                        view.hideWaitingDialog()
                    }
                    .subscribe({ bean ->
                        com.kit.utils.Logger.e(TAG, GsonUtil.obj2Json(bean))
                        if (null == bean) {
                        } else {
                            if (bean.code == ApiConstant.REQUEST_SUCCESS_CODE) {
                                AccountStore.saveAccountInfo(bean.data?.apply {
                                    this.phone = phoneNumber
                                })
                                if (!AccountStore.getImToken().isNullOrBlank()) {
                                    RongIM.connect(
                                        AccountStore.getImToken(),
                                        object : RongIMClient.ConnectCallback() {
                                            override fun onSuccess(t: String?) {
                                                view.hideWaitingDialog()
                                                view.onLoginSuccess()
                                            }

                                            override fun onError(e: RongIMClient.ConnectionErrorCode?) {
                                                view.hideWaitingDialog()
                                                e?.value?.let { view.showError(it, e.name) }
                                            }

                                            override fun onDatabaseOpened(code: RongIMClient.DatabaseOpenStatus?) {
                                            }
                                        })
                                }
                            } else {
                                view.showError(bean.code, bean.msg)
                            }
                        }
                    }, { throwable ->
                        view.showError(-1, throwable.message)
                    })
            )
        }
    }
}