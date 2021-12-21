/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroomdemo.mvp.model

import androidx.appcompat.app.AppCompatActivity
import cn.rongcloud.voiceroomdemo.net.CommonNetManager
import cn.rongcloud.voiceroomdemo.net.api.bean.request.GetVerificationCode
import cn.rongcloud.voiceroomdemo.net.api.bean.request.GetVerificationCodeInternational
import cn.rongcloud.voiceroomdemo.net.api.bean.request.LoginRequestBean
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.LoginRespondBean
import cn.rongcloud.voiceroomdemo.net.api.bean.respond.VerificationCodeRespondBean
import com.kit.utils.Logger
import com.rongcloud.common.AppConfig
import com.rongcloud.common.base.BaseLifeCycleModel
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
class LoginModel @Inject constructor(activity:AppCompatActivity):BaseLifeCycleModel(activity) {
    fun getVerificationCode(
        phoneNumber: String,
        region: String
    ): Single<VerificationCodeRespondBean> {
        Logger.e(
            TAG,
            "isInternationalization = " + AppConfig.isInternationalization + " region = " + region
        )
        if (AppConfig.isInternationalization) {
            var reg = region
            if (!region.startsWith("+")) {
                reg = "+" + region
            }
            Logger.e(TAG, "region = " + reg)
            return CommonNetManager.commonService.getVerificationCodeInternational(
                GetVerificationCodeInternational(phoneNumber, reg)
            ).observeOn(AndroidSchedulers.mainThread())
        } else {
            return CommonNetManager.commonService.getVerificationCode(
                GetVerificationCode(
                    phoneNumber
                )
            ).observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun login(phoneNumber: String, verifyCode: String): Single<LoginRespondBean> {
        if (AppConfig.isInternationalization) {
            return CommonNetManager.commonService.loginInternational(
                LoginRequestBean(
                    mobile = phoneNumber,
                    verifyCode = verifyCode,
                    deviceId = DeviceUtils.getDeviceId()
                )
            ).observeOn(AndroidSchedulers.mainThread())
        } else {
            return CommonNetManager.commonService.login(
                LoginRequestBean(
                    mobile = phoneNumber,
                    verifyCode = verifyCode,
                    deviceId = DeviceUtils.getDeviceId()
                )
            ).observeOn(AndroidSchedulers.mainThread())
        }

    }
}