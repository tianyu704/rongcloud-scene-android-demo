/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.common

import com.example.voiceroomdemo.MyApp
import com.example.voiceroomdemo.net.api.bean.respond.AccountInfo
import com.example.voiceroomdemo.utils.JsonUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

/**
 * @author gusd
 * @Date 2021/06/08
 */

private const val TAG = "AccountStore"

object AccountStore {

    private val EMPTY_ACCOUNT = AccountInfo()
    private val ACCOUNT_INFO by myStringPreferencesKey(EMPTY_ACCOUNT.toJson())

    private var currentInfo: AccountInfo

    init {
        currentInfo = JsonUtils.fromJson(
            MyApp.context.getValueSync(ACCOUNT_INFO),
            AccountInfo::class.java
        ) ?: EMPTY_ACCOUNT
    }


    public fun saveAccountInfo(info: AccountInfo?) {
        currentInfo = info ?: EMPTY_ACCOUNT
        MyApp.context.putValue(ACCOUNT_INFO, currentInfo.toJson())
    }

    public fun getAccountInfo(): AccountInfo = currentInfo


    public fun getImToken() = getAccountInfo().imToken


    public fun getUserName() = getAccountInfo().userName


    public fun getAuthorization() = getAccountInfo().authorization


    public fun getUserPortrait() = getAccountInfo().portrait

    public fun getUserId() = getAccountInfo().userId

    // 登出监听
    public fun obLogoutSubject(): Observable<Boolean> =
        MyApp.context.obValue(ACCOUNT_INFO)
            .filter {
                it.isNullOrEmpty() || it == ACCOUNT_INFO.defaultValue
            }.map {
                return@map true
            }.observeOn(AndroidSchedulers.mainThread())

    // 登出
    public fun logout() {

        saveAccountInfo(EMPTY_ACCOUNT)
    }

    // 监听账号信息发生变化
    public fun obAccountInfoChange(): Observable<AccountInfo> =
        MyApp.context.obValue(ACCOUNT_INFO).map {
            return@map JsonUtils.fromJson(it, AccountInfo::class.java) ?: EMPTY_ACCOUNT
        }.observeOn(AndroidSchedulers.mainThread())
}