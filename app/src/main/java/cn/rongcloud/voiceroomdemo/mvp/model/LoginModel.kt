/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.model

import cn.rongcloud.voiceroomdemo.net.api.bean.request.GetVerificationCode
import cn.rongcloud.voiceroomdemo.net.api.bean.request.LoginRequestBean
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.LoginRespondBean
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VerificationCodeRespondBean
import cn.rongcloud.voiceroomdemo.net.RetrofitManager
import com.rongcloud.common.utils.DeviceUtils
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * @author gusd
 * @Date 2021/06/07
 */
@ActivityScoped
class LoginModel @Inject constructor() {
    suspend fun getVerificationCode(phoneNumber: String): Single<VerificationCodeRespondBean> {
        return RetrofitManager.commonService.getVerificationCode(GetVerificationCode(phoneNumber))
            .observeOn(
                AndroidSchedulers.mainThread()
            )
    }

    suspend fun login(phoneNumber: String, verifyCode: String): Single<LoginRespondBean> {
        return RetrofitManager.commonService.login(
            LoginRequestBean(
                mobile = phoneNumber,
                verifyCode = verifyCode,
                deviceId = DeviceUtils.getDeviceId()
            )
        ).observeOn(AndroidSchedulers.mainThread())
    }
}